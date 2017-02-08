package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFValidator;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.apache.commons.codec.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * Unfinished
 */
public class VeraPDFInvokeMain {
    protected static final Logger log = LoggerFactory.getLogger(VeraPDFInvokeMain.class);

    public static final String VERAPDF_DATASTREAM_NAME = "VERAPDF";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerVeraPDFInvokeMain_VeraPdfTaskDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPDFInvokeModule.class, BitRepositoryModule.class})
    interface VeraPdfTaskDaggerComponent {
        Tool getTool();

    }

    @Module
    public static class VeraPDFInvokeModule {
        public static final String VERAPDF_INVOKED = "VeraPDF_Invoked";
        public static final String DPA_VERAPDF_FLAVOR = "dpa.verapdf.flavor";
        public static final String DPA_VERAPDF_REUSEEXISTINGDATASTREAM = "dpa.verapdf.reuseexistingdatastream";

        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(QuerySpecification query, DomsRepository domsRepository,
                                   EnhancedFedora efedora, DomsEventStorage<Item> domsEventStorage,
                                   @Named(BITMAG_BASEURL_PROPERTY) String bitrepositoryUrlPrefix,
                                   @Named(BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT) String bitrepositoryMountpoint,
                                   @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM) boolean reuseExistingDatastream,
                                   Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider) {

            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(domsItem -> processChildDomsId(domsEventStorage, bitrepositoryUrlPrefix, bitrepositoryMountpoint, veraPdfInvokerProvider, reuseExistingDatastream).apply(domsItem))
                    // Collect results for each domsId
                    .peek(o -> log.trace("Result: {}", o))
                    .collect(Collectors.toList())
                    .toString();

            return f;
        }

        private Function<DomsItem, String> processChildDomsId(DomsEventStorage<Item> domsEventStorage, String bitrepositoryUrlPrefix, String bitrepositoryMountpoint, Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider, boolean reuseExistingDatastream) {
            return domsItem -> {
                long startTime = System.currentTimeMillis();

                // Single doms item
                List<ToolResult> toolResults = domsItem.allChildren().stream()
                        .flatMap(childDomsItem -> invokeVeraPDFOnPhysicalFiles(childDomsItem, bitrepositoryUrlPrefix, bitrepositoryMountpoint, veraPdfInvokerProvider, reuseExistingDatastream))
                        .collect(Collectors.toList());

                // Sort according to result
                final Map<Boolean, List<ToolResult>> toolResultMap = toolResults.stream()
                        .collect(Collectors.groupingBy(tr -> tr.getResult()));

                List<ToolResult> failingToolResults = toolResultMap.getOrDefault(Boolean.FALSE, Collections.emptyList());

                String deliveryEventMessage = failingToolResults.stream()
                        .map(tr -> "---\n" + tr.getHumanlyReadableMessage() + "\n" + tr.getHumanlyReadableStackTrace())
                        .filter(s -> s.trim().length() > 0) // skip blank lines
                        .collect(Collectors.joining("\n"));

                // outcome was successful only if no toolResults has a FALSE result.
                boolean outcome = failingToolResults.size() == 0;

                final String keyword = getClass().getSimpleName();
                final Date timestamp = new Date();

                domsItem.appendEvent(keyword, timestamp, deliveryEventMessage, VERAPDF_INVOKED, outcome);

                log.info(KibanaLoggingStrings.FINISHED_DELIVERY_PDFINVOKE, domsItem.getDomsId().id(), (System.currentTimeMillis() - startTime));
                return domsItem + " processed. " + failingToolResults.size() + " failed. outcome = " + outcome;
            };
        }

        protected Stream<ToolResult> invokeVeraPDFOnPhysicalFiles(DomsItem domsItem, String bitrepositoryUrlPrefix, String bitrepositoryMountpoint, Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider, boolean reuseExistingDatastream) {

            final List<DomsDatastream> datastreams = domsItem.datastreams();

            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getMimeType().equals("application/pdf"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }
            log.trace("Found PDF datastream on {}", domsItem);

            if (reuseExistingDatastream) {
                if (datastreams.stream().filter(ds -> ds.getID().equals(VERAPDF_DATASTREAM_NAME)).findAny().isPresent()) {
                    log.trace("Reused existing VERAPDF datastream for {}", domsItem);
                    return Stream.of(ToolResult.ok("Reused existing VERAPDF datastream for " + domsItem));
                }
            }

            DomsDatastream ds = profileOptional.get();

            // We need to locate the physical file containing the PDF.  For the bitrepository
            // the official way in is rather complicated and slow, but for the SB pillar we know
            // where the information is stored for deriving the file name directly.

            // @kfc: Det autoritative svar er at laese url'en som content peger paa, og fjerne det faste
            // bitrepositoryUrlPrefix: http://bitfinder.statsbiblioteket.dk/<collection>/
            final String url = ds.getUrl();
            if (url.startsWith(bitrepositoryUrlPrefix) == false) {
                return Stream.of(ToolResult.fail(domsItem + " url '" + url + " does not start with '" + bitrepositoryUrlPrefix + "'"));
            }
            String filename = url.substring(bitrepositoryUrlPrefix.length());  // FIXME:  Sanity check input
            File file = null;
            try {
                Path path = Paths.get(bitrepositoryMountpoint, URLDecoder.decode(filename, CharEncoding.UTF_8));
                file = path.toFile();
                log.trace("pdf expected to be in:  {}", file.getAbsolutePath());
            } catch (UnsupportedEncodingException e) {
                return Stream.of(ToolResult.fail(domsItem + " file '" + filename + " could not get decoded", e));
            }

            long startTime = System.currentTimeMillis();

            byte[] veraPDF_output;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                veraPDF_output = veraPdfInvokerProvider.get().apply(inputStream);
            } catch (FileNotFoundException e) {
                return Stream.of(ToolResult.fail(domsItem + " file '" + file.getAbsolutePath() + " does not exist", e));
            } catch (Exception e) {
                return Stream.of(ToolResult.fail(domsItem + " file '" + file.getAbsolutePath() + " failed validation", e));
            }
            log.info(KibanaLoggingStrings.FINISHED_FILE_PDFINVOKE, file.getAbsolutePath(), (System.currentTimeMillis() - startTime));

            // We have now run VeraPDF on the PDF file and has the output in hand.
            // Store it in the "VERAPDF" datastream for the object.
            // Unfortunately ObjectProfile does not have a method for this, so we ask Fedora directly.

            String comment = file.getAbsolutePath() + " at " + new java.util.Date();
            try {
                domsItem.modifyDatastreamByValue(
                        VERAPDF_DATASTREAM_NAME,
                        null, // no checksum
                        null, // no checksum
                        veraPDF_output,
                        null,
                        "text/xml",
                        comment,
                        null);
            } catch (Exception e) {
                return Stream.of(ToolResult.fail(domsItem + " file '" + file.getAbsolutePath() + "' could not save to datastream"));
            }
            return Stream.of(ToolResult.ok(domsItem + " " + comment));
        }

        @Provides
        protected Function<QuerySpecification, Stream<DomsId>> sboiEventIndexSearch(SBOIEventIndex<Item> index) {
            return query -> sboiEventIndexSearch(query, index).stream();
        }

        private List<DomsId> sboiEventIndexSearch(QuerySpecification query, SBOIEventIndex<Item> index) {
            Iterator<Item> iterator;
            try {
                EventTrigger.Query<Item> q = new EventTrigger.Query<>();
                q.getPastSuccessfulEvents().addAll(query.getPastSuccessfulEvents());
                q.getOldEvents().addAll(query.getOldEvents());
                q.getFutureEvents().addAll(query.getFutureEvents());
                q.getTypes().addAll(query.getTypes());
                iterator = index.search(false, q);
            } catch (CommunicationException e) {
                throw new RuntimeException("sboiEventIndexSearch()", e);
            }
            // http://stackoverflow.com/a/28491752/53897
            // To keep this simple we simply read in the whole result in a list.
            List<DomsId> l = new ArrayList<>();
            iterator.forEachRemaining(item -> l.add(new DomsId(item.getDomsID())));
            return l;
        }

        @Provides
        @Named("pageSize")
        Integer providePageSize(ConfigurationMap map) {
            return Integer.valueOf(map.getRequired("pageSize"));
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

        @Provides
        @Named(DPA_VERAPDF_FLAVOR)
        String getVeraPDFFlavor(ConfigurationMap map) {
            return map.getRequired(DPA_VERAPDF_FLAVOR);
        }

        @Provides
        Function<InputStream, byte[]> getVeraPDFInvoker(@Named(DPA_VERAPDF_FLAVOR) String flavorId) {
            return new VeraPDFValidator(flavorId, true);
        }

        @Provides
        @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM)
        boolean provideReuseExistingDatastream(ConfigurationMap map) {
            return Boolean.valueOf(map.getDefault(DPA_VERAPDF_REUSEEXISTINGDATASTREAM, "false"));
        }
    }
}
