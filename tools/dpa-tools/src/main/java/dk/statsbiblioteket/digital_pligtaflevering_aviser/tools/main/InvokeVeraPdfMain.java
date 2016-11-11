package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.TaskResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf.VeraPDFValidator;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPdfModule.class})
    interface VeraPdfTaskDaggerComponent {
        Tool getTool();
    }

    @Module
    public static class VeraPdfModule {

        @Provides
//        Runnable provideRunnable(Modified_SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        protected Tool provideTool(Stream<DomsId> domsIdStream, EnhancedFedora efedora) { //}, Task<DomsItem, ObjectProfile> task) {

            Stream<DomsId> childrenStream = domsIdStream
                    .peek(System.out::println)
                    .flatMap(domsId -> findAllChildrenFor(domsId).stream());

            Object result = childrenStream
                    .peek(System.out::println)
                    .flatMap(domsId -> analyzePDF(domsId, efedora))
                    .collect(Collectors.toList());
            // FIXME:  Aggregate results and store in event on batch(?)/newspaper(?) DOMS node?
            return () -> log.info("Result: {}", result);
        }

        private Stream<TaskResult> analyzePDF(DomsId domsId, EnhancedFedora efedora) {

            ObjectProfile op = getObjectProfileFor(domsId, efedora);

            Optional<DatastreamProfile> profileOptional = op.getDatastreams().stream()
                    .filter(ds -> ds.getMimeType().equals("application/pdf"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }

            DatastreamProfile ds = profileOptional.get();
            // @kfc: Det autoritative svar er at laese url'en som content peger paa, og fjerne det faste prefix: http://bitfinder.statsbiblioteket.dk/<collection>/
            String prefix = "http://localhost:58709/";
            final String url = ds.getUrl();
            if (url.startsWith(prefix) == false) {
                return Stream.of(new TaskResult(false, "id: " + domsId + " url '" + url + " does not start with '" + prefix + "'"));
            }
            String filename = url.substring(prefix.length());  // FIXME:  Sanity check input

            Path path = Paths.get("/home/tra/git/digital-pligtaflevering-aviser-tools/bitrepositorystub-storage", filename);
            File file = path.toFile();
            log.trace("validating pdf:  {}", file.getAbsolutePath());

            byte[] veraPDF_output;
            VeraPDFValidator validator = new VeraPDFValidator("1a", true);
            try {
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
                efedora.modifyDatastreamByValue(domsId.id(),
                        "VERAPDF",
                        null, // no checksum
                        null, // no checksum
                        veraPDF_output,
                        null,
                        "text/xml",
                        comment,
                        null);
            } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
                return Stream.of(new TaskResult(false, "id: " + domsId + " file '" + file.getAbsolutePath() + "' could not save to datastream"));
            }

            // FIXME:  Set event on this DOMS node (for the individual page) saying VERAPDF datastream is available.

            return Stream.of(new TaskResult(true, "id: " + domsId + " " + comment));
        }

        protected List<DomsId> findAllChildrenFor(DomsId rootDomsId) {
            List<DomsId> found = new ArrayList<>();  // maintain the order found in
            List<DomsId> unprocessed = new ArrayList<>();
            unprocessed.add(rootDomsId);
            while (unprocessed.isEmpty() == false) {
                DomsId currentId = unprocessed.remove(0);
                found.add(currentId);
                log.debug("Finding children for {}", currentId);
                //
                String restUrl = "http://localhost:7880/fedora/objects";

                Client client = Client.create();
                client.addFilter(new HTTPBasicAuthFilter("fedoraAdmin", "fedoraAdminPass"));

                // Logic lifted from https://github.com/statsbiblioteket/newspaper-batch-event-framework/blob/master/newspaper-batch-event-framework/tree-processor/src/main/java/dk/statsbiblioteket/medieplatform/autonomous/iterator/fedora3/IteratorForFedora3.java#L146
                final WebResource webResource = client.resource(restUrl).path(currentId.id()).path("relationships").queryParam("format", "ntriples");

                log.trace("WebResource: {}", webResource.getURI());
                //webResource.addFilter(new LoggingFilter(System.err));
//<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/relations-external#hasPart> <info:fedora/uuid:5cfa5459-c553-4894-980e-b2b7b37b37d3> .
//<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_DOMS> .
//<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <http://doms.statsbiblioteket.dk/relations/default/0/1/#isPartOfCollection> <info:fedora/doms:Newspaper_Collection> .
//<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_Item> .
//<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_RoundTrip> .
//<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/fedora-system:FedoraObject-3.0> .
                String result = webResource.get(String.class);
                for (String s : result.split("\n")) {
                    //<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/relations-external#hasPart> <info:fedora/uuid:5cfa5459-c553-4894-980e-b2b7b37b37d3> .
                    String[] tuple = s.split(" ");
                    if (tuple.length >= 3 && tuple[2].startsWith("<info:fedora/")) {
                        // To be compatible with existing configuration files
                        String predicate = tuple[1].substring(1, tuple[1].length() - ">".length());
                        String child = tuple[2].substring("<info:fedora/".length(), tuple[2].length() - ">".length());
                        // ConfigConstants.ITERATOR_DOMS_PREDICATENAMES
                        if (predicate.equals("info:fedora/fedora-system:def/relations-external#hasPart")) {
                            final DomsId childDomsId = new DomsId(child);
                            if (found.contains(childDomsId)) {
                                // seen before - should not happen in a tree, but ignore it just to be certain
                            } else {
                                unprocessed.add(childDomsId);
                            }
                        }
                    }
                }
            }
            return found;
        }

        protected ObjectProfile getObjectProfileFor(DomsId domsId, EnhancedFedora efedora) {
            try {
                ObjectProfile objectProfile = efedora.getObjectProfile(domsId.id(), null);
                return objectProfile;
            } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
                throw new RuntimeException("could not getObjectProfile() for domsId " + domsId, e);
            }
        }

        @Provides
        protected Stream<DomsId> sboiEventIndexSearch(QuerySpecification query, SBOIEventIndex<Item> index) {
            Iterator iterator;
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
            // http://stackoverflow.com/a/29010716/53897
            Iterable iterable = () -> iterator;
            Stream<Item> itemStream = StreamSupport.stream(iterable.spliterator(), false);
            // convert to DomsID("....") stream.
            Stream<DomsId> domsIdStream = itemStream
                    .peek(i -> System.out.println(i))
                    .map(i -> new DomsId(i.getDomsID()));
            return domsIdStream;
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
        Stream<EventTrigger.Query<Item>> provideQueryStream() {
            //System.out.println("In provideQueryStream()");

            EventTrigger.Query<Item> query1 = new EventTriggerQuery<>("query1");
            // Metadata_Archived,Data_Archived
            query1.getPastSuccessfulEvents().add("Metadata_Archived");
            query1.getPastSuccessfulEvents().add("Data_Archived");
            query1.getTypes().add("doms:ContentModel_RoundTrip");
//            EventTrigger.Query<Item> query2 = new EventTriggerQuery<>("query2");
//            query2.getPastSuccessfulEvents().add("Data_Received");
//            query2.getFutureEvents().add("Metadata_Archived");
            return Stream.of(query1);
        }

        protected class EventTriggerQuery<I extends Item> extends EventTrigger.Query<I> {
            private String description;

            public EventTriggerQuery(String description) {
                this.description = description;
            }

            @Override
            public String toString() {
                return description + ": " + super.toString();
            }
        }
    }

    private static Date appendTaskResultToItem(DomsEventStorage<Item> domsEventStorage, DomsId domsID, TaskResult taskResult, String eventType) {
        try {
            final Item item = new Item(domsID.id());
            item.setEventList(new ArrayList<>());
            return domsEventStorage.appendEventToItem(item, "agent", new Date(), taskResult.getResult(), eventType, taskResult.isSuccess());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
