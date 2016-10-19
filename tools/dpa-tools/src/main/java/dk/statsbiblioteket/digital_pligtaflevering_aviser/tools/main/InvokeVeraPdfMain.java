package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Task;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.AutonomousPreservationToolComponent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Unfinished
 */
public class InvokeVeraPdfMain {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerInvokeVeraPdfMain_VeraPdfTaskComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, VeraPdfModule.class})
    interface VeraPdfTaskComponent extends AutonomousPreservationToolComponent {
    }

    @Module
    public static class VeraPdfModule {

        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
//        Runnable provideRunnable(SBOIEventIndex index, DomsEventStorage<Item> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        Runnable provideRunnable(SBOIEventIndex<Item> index, Stream<EventTrigger.Query<Item>> queryStream, Task task) {
            return () -> queryStream
                    .peek(query -> log.info("Query: {}", query))
                    .map(  // apply to each item
                            query -> {
                                return sboiEventIndexSearch(index, query)
                                        .peek(item ->
                                                log.info("Item: {}", item)
                                        )
                                        .map(task)
                                        .peek(result -> log.info("Result: {}", result))
                                        .collect(Collectors.toList());
                            }
                    )
                    .forEach(result -> log.info("Result: {}", result))
                    ;
        }

        protected Stream<Item> sboiEventIndexSearch(SBOIEventIndex<Item> index, EventTrigger.Query<Item> query) {
            Iterator iterator;
            try {
                iterator = index.search(true, query);
            } catch (CommunicationException e) {
                throw new RuntimeException("index.search(...)", e);
            }
            // http://stackoverflow.com/a/29010716/53897
            Iterable iterable = () -> iterator;
            return StreamSupport.stream(iterable.spliterator(), false);
        }

        @Provides
        @Named("pageSize")
        Integer providePageSize(ConfigurationMap map) {
            return Integer.valueOf(map.getRequired("pageSize"));
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item(id);
        }

        @Provides
        Task getTask() {
            return item -> "ok"; // dummy getTool.
        }

        @Provides
        Stream<EventTrigger.Query<Item>> provideQueryStream() {
            //System.out.println("In provideQueryStream()");

            EventTrigger.Query<Item> query1 = new EventTriggerQuery<>("query1");
            // Metadata_Archived,Data_Archived
            query1.getPastSuccessfulEvents().add("Metadata_Archived");
            query1.getPastSuccessfulEvents().add("Data_Archived");
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
