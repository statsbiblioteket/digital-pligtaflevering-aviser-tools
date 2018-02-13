package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.google.common.base.Throwables;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResultsReport;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.streams.TupleElement;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsValue;
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
import javaslang.control.Either;
import org.apache.commons.codec.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Provider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE;
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
                m -> dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.DaggerVeraPDFInvokeMain_VeraPdfTaskDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPDFInvokeModule.class, BitRepositoryModule.class})
    interface VeraPdfTaskDaggerComponent {
        Tool getTool();

    }

    /**
     * @noinspection WeakerAccess
     */
    @Module
    public static class VeraPDFInvokeModule {
        public static final String VERAPDF_INVOKED = "VeraPDF_Invoked";
        public static final String DPA_VERAPDF_FLAVOR = "dpa.verapdf.flavor";
        public static final String DPA_VERAPDF_REUSEEXISTINGDATASTREAM = "dpa.verapdf.reuseexistingdatastream";

        /** @noinspection PointlessBooleanExpression*/
        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                   QuerySpecification workToDoQuery,
                                   DomsRepository domsRepository,
                                   EnhancedFedora efedora,
                                   DomsEventStorage<Item> domsEventStorage,
                                   @Named(BITMAG_BASEURL_PROPERTY) String bitrepositoryUrlPrefix,
                                   @Named(BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT) String bitrepositoryMountpoint,
                                   @Named(DPA_VERAPDF_REUSEEXISTINGDATASTREAM) boolean reuseExistingDatastream,
                                   Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider,
                                   DefaultToolMXBean mxBean) {

            final String agent = getClass().getSimpleName();

            Tool f = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(DomsValue::create)
                    .map(c -> c.map(item -> processChildDomsId(domsEventStorage, bitrepositoryUrlPrefix, bitrepositoryMountpoint, veraPdfInvokerProvider, reuseExistingDatastream, mxBean).apply(item)))
                    // Collect results for each domsId
                    .peek(c -> {
                        final DomsItem item = c.left();
                        final Either<Exception, ToolResult> value = c.right();
                        if (value.isLeft()) {
                            // Processing of _this_ domsItem threw unexpected exception
                            item.appendEvent(new DomsEvent(agent, new Date(), TupleElement.stacktraceFor(value.getLeft()), eventName, false));
                        } else {
                            final ToolResult toolResult = (ToolResult) value.get();
                            item.appendEvent(new DomsEvent(agent, new Date(), toolResult.getHumanlyReadableMessage(), eventName, toolResult.isSuccess()));
                            if (toolResult.isSuccess() == false) {
                                item.appendEvent(new DomsEvent(agent, new Date(), "autonomous component failed", STOPPED_STATE, false));
                            }
                        }
                    })
                    .peek(o -> log.trace("Result: {}", o))
                    .count() + " items processed";  // FIXME:  Better message from list.

            return f;
        }

        private Function<DomsItem, Either<Exception, ToolResult>> processChildDomsId(DomsEventStorage<Item> domsEventStorage,
                                                                                     String bitrepositoryUrlPrefix,
                                                                                     String bitrepositoryMountpoint,
                                                                                     Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider,
                                                                                     boolean reuseExistingDatastream,
                                                                                     DefaultToolMXBean mxBean) {
            return domsItem -> {
                try {
                    long startTime = System.currentTimeMillis();

                    List<TupleElement<DomsItem, Either<Exception, ToolResult>>> toolResults = domsItem.allChildren()
                            .peek(i -> mxBean.currentId = String.valueOf(i))
                            .peek(i -> mxBean.idsProcessed++)
                            .map(DomsValue::create)
                            .flatMap((c) -> c.flatMap(invokeVeraPDFOnPhysicalFiles0(bitrepositoryUrlPrefix, bitrepositoryMountpoint, veraPdfInvokerProvider, reuseExistingDatastream)))
                            .collect(Collectors.toList());

                    ToolResultsReport<DomsItem> trr = new ToolResultsReport<>(
                            new ToolResultsReport.OK_COUNT_FAIL_LIST_RENDERER<>(),
                            (id, t) -> log.error("id: {}", id, t),
                            Throwables::getStackTraceAsString);

                    ToolResult result = trr.apply(domsItem, toolResults);

                    log.info(KibanaLoggingStrings.FINISHED_DELIVERY_PDFINVOKE, domsItem.getDomsId().id(), (System.currentTimeMillis() - startTime));

                    return Either.right(result);
                } catch (Exception e) {
                    return Either.left(e);
                }
            };
        }

        public Function<DomsItem, Stream<Either<Exception, ToolResult>>> invokeVeraPDFOnPhysicalFiles0(String bitrepositoryUrlPrefix, String bitrepositoryMountpoint, Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider, boolean reuseExistingDatastream) {
            return childDomsItem -> {
                try {
                    return invokeVeraPDFOnPhysicalFiles1(childDomsItem, bitrepositoryUrlPrefix, bitrepositoryMountpoint, veraPdfInvokerProvider, reuseExistingDatastream).map(Either::right);
                } catch (Exception e) {
                    return Stream.of(Either.left(e));
                }
            };
        }

        protected Stream<ToolResult> invokeVeraPDFOnPhysicalFiles1(DomsItem domsItem, String bitrepositoryUrlPrefix, String bitrepositoryMountpoint, Provider<Function<InputStream, byte[]>> veraPdfInvokerProvider, boolean reuseExistingDatastream) {

            log.trace("Inspecting {} for datastreams", domsItem);

            final List<DomsDatastream> datastreams = domsItem.datastreams();

            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getMimeType().equals("application/pdf"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }
            log.trace("Found PDF datastream on {}", domsItem);

            if (reuseExistingDatastream) {
                if (datastreams.stream().anyMatch(ds -> ds.getId().equals(VERAPDF_DATASTREAM_NAME))) {
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

            // If the URL starts with bitrepositoryUrlPrefix we rewrite the URL to look for
            // a local file (mounted from Isilon).  If not we process the URL given (which
            // for non-local bitrepositories might be abysmally slow).

            final InputStream inputStream;
            final String resourceName;

            final String url = ds.getUrl();
            if (url.startsWith(bitrepositoryUrlPrefix)) {
                // We have an URL pointing to a
                if (url.length() < bitrepositoryUrlPrefix.length()) {
                    return Stream.of(ToolResult.fail(" url '" + url + "' shorter than bitrepositoryUrlPrefix"));
                }
                resourceName = url.substring(bitrepositoryUrlPrefix.length());
                final File file;
                try {
                    Path path = Paths.get(bitrepositoryMountpoint, URLDecoder.decode(resourceName, CharEncoding.UTF_8));
                    file = path.toFile();
                    log.trace("pdf expected to be in:  {}", file.getAbsolutePath());
                    inputStream = new FileInputStream(file);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(domsItem + " '" + resourceName + "' could not get decoded", e);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(domsItem + " '" + resourceName + "' not found", e);
                }
            } else {
                try {
                    //return Stream.of(ToolCompletedResult.fail(domsItem + " url '" + url + " does not start with '" + bitrepositoryUrlPrefix + "'"));
                    resourceName = url;
                    inputStream = new URL(url).openStream();
                } catch (IOException e) {
                    throw new RuntimeException(domsItem + " url '" + url + " fails", e);
                }
            }

            long startTime = System.currentTimeMillis();

            byte[] veraPDF_output;
            try (InputStream inputStreamForVeraPDF = inputStream) {
                // actual work starts
                veraPDF_output = veraPdfInvokerProvider.get().apply(inputStreamForVeraPDF);
                // actual work ends.
            } catch (Exception e) {
                throw new RuntimeException(domsItem + " " + resourceName + " failed validation", e);
            }

            log.info(KibanaLoggingStrings.FINISHED_FILE_PDFINVOKE, resourceName, (System.currentTimeMillis() - startTime));

            // We have now run VeraPDF on the PDF file and has the output in hand.
            // Store it in the "VERAPDF" datastream for the object.
            // Unfortunately ObjectProfile does not have a method for this, so we ask Fedora directly.

            String comment = resourceName + " at " + new java.util.Date();

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
                throw new RuntimeException(domsItem + " '" + resourceName + "' could not save to datastream");
            }
            return Stream.of(ToolResult.ok(comment));
        }

        @Provides
        protected Function<EventQuerySpecification, Stream<DomsId>> sboiEventIndexSearch(SBOIEventIndex<Item> index) {
            return query -> sboiEventIndexSearch(query, index).stream();
        }

        private List<DomsId> sboiEventIndexSearch(EventQuerySpecification query, SBOIEventIndex<Item> index) {
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
