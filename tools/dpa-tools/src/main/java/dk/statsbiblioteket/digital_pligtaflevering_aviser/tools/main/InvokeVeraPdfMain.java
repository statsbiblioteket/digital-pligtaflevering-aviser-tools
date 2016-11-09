package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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

            Object result = domsIdStream
                    .peek(System.out::println)
                    .flatMap(domsId -> findAllChildrenFor(domsId).stream())
                    .peek(System.out::println)
                    .map(domsId -> getObjectProfileFor(domsId, efedora))
                    // Does object profile have a PDF typed stream?
                    .filter(op -> op.getDatastreams().stream().filter(ds -> ds.getMimeType().equals("application/pdf")).count() > 0)
                    // We now have the ObjectProfiles for which there is an PDF datastream.
                    .map(x -> {
                        for (DatastreamProfile ds : x.getDatastreams()) {
                            if (ds.getMimeType().equals("application/pdf")) {
                                return Arrays.asList(x, ds.getLabel());
                            }
                        }
                        throw new RuntimeException("hey");
                    })
                    .collect(Collectors.toList());
            return () -> log.info("Result: {}", result);
        }

        private Set<DomsId> findAllChildrenFor(DomsId rootDomsId) {
            Set<DomsId> found = new HashSet<>();
            List<DomsId> unprocessed = new ArrayList<>(); // Does DOMS prefer a given order?
            unprocessed.add(rootDomsId);
            while (unprocessed.isEmpty() == false) {
                DomsId currentId = unprocessed.remove(0);
                found.add(currentId);
                log.trace("Processing {}", currentId);
                //
                String restUrl = "http://localhost:7880/fedora/objects";

                Client client = Client.create();
                client.addFilter(new HTTPBasicAuthFilter("fedoraAdmin", "fedoraAdminPass"));

                // https://github.com/statsbiblioteket/newspaper-batch-event-framework/blob/master/newspaper-batch-event-framework/tree-processor/src/main/java/dk/statsbiblioteket/medieplatform/autonomous/iterator/fedora3/IteratorForFedora3.java#L146
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
                        String predicate = tuple[1].substring(1, tuple[1].length() - ">".length());
                        String child = tuple[2].substring("<info:fedora/".length(), tuple[2].length() - ">".length());
                        // ConfigConstants.ITERATOR_DOMS_PREDICATENAMES
                        if (predicate.equals("info:fedora/fedora-system:def/relations-external#hasPart")) {
                            final DomsId childDomsId = new DomsId(child);
                            if (found.contains(childDomsId)) {
                                // seen before - should not happen in a tree, but just to be certain
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
                ObjectProfile xxx = efedora.getObjectProfile(domsId.id(), null);
                return xxx;
            } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
                e.printStackTrace();
            }
            return null;
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
//        Task getTask(DomsEventStorage<Item> domsEventStorage, EnhancedFedora efedora) {
        Function<String, DomsItem> getDomsItemMapper(DomsEventStorage<Item> domsEventStorage, EnhancedFedora efedora) {
//            return item -> new DomsItem(item, domsEventStorage);
//            final Task<DomsItem, ObjectProfile> domsItemObjectProfileTask = item -> {
//                try {
//                    final String domsId = "uuid:a58ed278-e20f-4505-84a2-59ae8d8a8777";
//                    final Item itemFromDomsID = domsEventStorage.getItemFromDomsID(domsId);
//                    final ObjectProfile objectProfile = efedora.getObjectProfile(domsId, null);
//                    return objectProfile;
//                } catch (CommunicationException | NotFoundException | BackendInvalidCredsException
//                        | BackendMethodFailedException | BackendInvalidResourceException e) {
//                    throw new RuntimeException(e);
//                }
//            };
//            return domsItemObjectProfileTask;
            throw new RuntimeException("commented out");
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

//    private static Date appendEventToItem(DomsEventStorage<Item> domsEventStorage, Item item) {
//        try {
//            return domsEventStorage.appendEventToItem(item, "agent", new Date(), "details", "T" + item.getEventList().size(), false);
//        } catch (RuntimeException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
