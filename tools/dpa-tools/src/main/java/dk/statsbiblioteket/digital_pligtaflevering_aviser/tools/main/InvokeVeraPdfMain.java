package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.InfomediaBatch;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
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
import java.util.Iterator;
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
        protected Tool provideTool(Stream<DomsId> domsIdStream, EnhancedFedora efedora){ //}, Task<DomsItem, ObjectProfile> task) {

            Object result = domsIdStream
                    .peek(System.out::println)
                    .map(domsId -> new InfomediaBatch(domsId, efedora))
                    .flatMap(batch -> batch.getSingleDayNewspaperStream()
                        .map(singleDay -> singleDay.getInfomediaSinglePagePDFStream().collect(Collectors.toList())
                        ))
                    .peek(System.out::println)
                    .collect(Collectors.toList());
            return () -> log.info("Result: {}", result);
        }

        protected ObjectProfile processSingleDomsId(DomsId domsId, EnhancedFedora efedora) {
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
