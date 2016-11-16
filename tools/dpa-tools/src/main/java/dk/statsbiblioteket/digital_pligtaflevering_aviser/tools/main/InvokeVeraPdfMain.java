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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.TaskResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFValidator;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.NotFoundException;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * Unfinished
 */
public class InvokeVeraPdfMain {
    protected static final Logger log = LoggerFactory.getLogger(InvokeVeraPdfMain.class);

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerInvokeVeraPdfMain_VeraPdfTaskDaggerComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPdfModule.class, BitRepositoryModule.class})
    interface VeraPdfTaskDaggerComponent {
        Tool getTool();
    }

    @Module
    public static class VeraPdfModule {

        public static final String EVENTTYPE = "EVENTTYPE";
        public static final String AGENT = "agent";
        public static final String DPA_PUTFILE_DESTINATIONPATH = "dpa.putfile.destinationpath";
        public static final String DPA_VERAPDF_FLAVOR = "dpa.verapdf.flavor";

        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(QuerySpecification query, DomsRepository domsRepository,
                                   EnhancedFedora efedora, DomsEventStorage<Item> domsEventStorage,
                                   @Named(BITMAG_BASEURL_PROPERTY) String bitrepositoryUrlPrefix,
                                   @Named(DPA_PUTFILE_DESTINATIONPATH) String bitrepositoryMountpoint,
                                   @Named(DPA_VERAPDF_FLAVOR) String flavorId) {

            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("{}", o))
                    .map(domsId -> processChildDomsId(domsRepository, domsEventStorage, bitrepositoryUrlPrefix, bitrepositoryMountpoint, flavorId).apply(domsId))
                    // Collect results for each domsId
                    .peek(o -> log.trace("{}", o))
                    .collect(Collectors.toList())
                    .toString();

            return f;
        }

        private static Function<DomsId, String> processChildDomsId(DomsRepository domsRepository, DomsEventStorage<Item> domsEventStorage, String bitrepositoryUrlPrefix, String bitrepositoryMountpoint, String flavorId) {
            return domsId -> {
                // Single doms item
                List<TaskResult> taskResults = domsRepository.allChildrenFor(domsId).stream()
                        .peek(System.out::println)
                        .flatMap(childDomsId -> analyzePDF(childDomsId, domsRepository, bitrepositoryUrlPrefix, bitrepositoryMountpoint, flavorId))
                        .collect(Collectors.toList());

                List<String> failedTaskResults = taskResults.stream()
                        .filter(t -> t.isSuccess() == false)
                        .map(t -> t.getResult())
                        .collect(Collectors.toList());

                final boolean success = failedTaskResults.size() == 0;
                String eventDetails;
                String eventFullDetails;
                if (success) {
                    eventDetails = "All " + taskResults.size() + " successful.";
                    eventFullDetails = eventDetails;
                } else {
                    eventDetails = failedTaskResults.size() + " failed out of " + taskResults.size();
                    eventFullDetails = eventDetails + "\n\n" + String.join("\n", failedTaskResults);
                }

                log.info("DomsID {}: {}", domsId, eventDetails);

                final Item fakeItemToGetAroundAPI = new Item(domsId.id());
                fakeItemToGetAroundAPI.setEventList(Collections.emptyList());
                final Date timestamp = new Date();
                try {
                    // FIXME:  Migrate events into API.
                    domsEventStorage.appendEventToItem(fakeItemToGetAroundAPI, AGENT, timestamp, eventFullDetails, EVENTTYPE, success);
                } catch (CommunicationException | NotFoundException e) {
                    throw new RuntimeException("Could not store event for domsId " + domsId, e);
                }
                return domsId + " " + eventDetails;
            };
        }

        private static Stream<TaskResult> analyzePDF(DomsId domsId, DomsRepository domsRepository, String bitrepositoryUrlPrefix, String bitrepositoryMountpoint, String flavorId) {

            DomsItem domsItem = domsRepository.lookup(domsId);
            Optional<DomsDatastream> profileOptional = domsItem.datastreams().stream()
                    .filter(ds -> ds.getMimeType().equals("application/pdf"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }

            DomsDatastream ds = profileOptional.get();
            // @kfc: Det autoritative svar er at laese url'en som content peger paa, og fjerne det faste
            // bitrepositoryUrlPrefix: http://bitfinder.statsbiblioteket.dk/<collection>/
            final String url = ds.getUrl();
            if (url.startsWith(bitrepositoryUrlPrefix) == false) {
                return Stream.of(new TaskResult(false, "id: " + domsId + " url '" + url + " does not start with '" + bitrepositoryUrlPrefix + "'"));
            }
            String filename = url.substring(bitrepositoryUrlPrefix.length());  // FIXME:  Sanity check input

            Path path = Paths.get(bitrepositoryMountpoint, filename);
            File file = path.toFile();
            log.trace("validating pdf:  {}", file.getAbsolutePath());

            byte[] veraPDF_output;
            try {
                VeraPDFValidator validator = new VeraPDFValidator(flavorId, true);
                veraPDF_output = validator.apply(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                return Stream.of(new TaskResult(false, "id: " + domsId + " file '" + file.getAbsolutePath() + " does not exist", e));
            } catch (Exception e) {
                return Stream.of(new TaskResult(false, "id: " + domsId + " file '" + file.getAbsolutePath() + " failed validation", e));
            }
            // We have now run VeraPDF on the PDF file and has the output in hand.
            // Store it in the "VERAPDF" datastream for the object.
            // Unfortunately ObjectProfile does not have a method for this, so we ask Fedora directly.

            String comment = file.getAbsolutePath() + " at " + new java.util.Date();
            try {
                domsItem.modifyDatastreamByValue(
                        "VERAPDF",
                        null, // no checksum
                        null, // no checksum
                        veraPDF_output,
                        null,
                        "text/xml",
                        comment,
                        null);
            } catch (Exception e) {
                return Stream.of(new TaskResult(false, "id: " + domsId + " file '" + file.getAbsolutePath() + "' could not save to datastream"));
            }
            // FIXME:  Do we need to store an event on the individual PDF node?
            return Stream.of(new TaskResult(true, "id: " + domsId + " " + comment));
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
        @Named(DPA_PUTFILE_DESTINATIONPATH)
        String getPutfileDestinationPath(ConfigurationMap map) {
            return map.getRequired(DPA_PUTFILE_DESTINATIONPATH);
        }

        @Provides
        @Named(DPA_VERAPDF_FLAVOR)
        String getVeraPDFFlavor(ConfigurationMap map) {
            return map.getRequired(DPA_VERAPDF_FLAVOR);
        }
    }
}
