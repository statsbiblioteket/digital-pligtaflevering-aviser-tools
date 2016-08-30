package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.LoggingFaultBarrier;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Task;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;

/**
 *
 */
public class VeraPdfMain {
    public static void main(String[] args) {
        // initial testing map
        Map<String, String> map = new HashMap<>();
        map.put(AUTONOMOUS_SBOI_URL, "http://localhost:58608/newspapr/sbsolr/");
        map.put(DOMS_URL, "http://localhost:7880/fedora");
        map.put(DOMS_PIDGENERATOR_URL, "http://localhost:7880/pidgenerator-service");
        map.put("pageSize", "10");
        map.put(DOMS_USERNAME, "not used");
        map.put(DOMS_PASSWORD, "not used");

        // first let dagger get up and running
        VeraPdfTaskComponent taskComponent = DaggerVeraPdfTaskComponent.builder()
                .configurationMap(new ConfigurationMap(map))
                .build();

        // then ask for the runnable constructed by dagger
        Runnable runnable = taskComponent.getRunnableTask();

        // and run it inside an appropriate fault barrier
        new LoggingFaultBarrier(runnable).run();
    }
}

@Singleton // FIXME
@Component(modules = {ConfigurationMap.class, DomsModule.class, VeraPdfModule.class})
interface VeraPdfTaskComponent {
    Runnable getRunnableTask();
};

@Singleton // FIXME
@Module
class VeraPdfModule {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Provides
    Runnable provideRunnable(Lazy<SBOIEventIndex> index, Lazy<DomsEventStorage<Item>> domsEventStorage, Stream<EventTrigger.Query> queryStream, Task task) {
        return () -> queryStream
                .peek(query -> log.info("Query: {}", query))
                .map(  // apply to each item
                        query -> sboiEventIndexSearch(index.get(), query)
                                .peek(item -> log.info("Item: {}", item))
                                .map(task)
                                .peek(result -> log.info("Result: {}", result))
                                .collect(Collectors.toList())
                )
                .forEach(result -> log.info("Result: {}", result))
                ;
    }

    protected Stream<Item> sboiEventIndexSearch(SBOIEventIndex index, EventTrigger.Query query) {
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

    private static Date appendEventToItem(DomsEventStorage domsEventStorage, Item item) {
        try {
            return domsEventStorage.appendEventToItem(item, "agent", new Date(), "details", "T" + item.getEventList().size(), false);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Named("pageSize")
    Integer providePageSize(ConfigurationMap map) {
        return Integer.valueOf(Objects.requireNonNull(map.get("pageSize"), "pageSize"));
    }

    @Provides
    ItemFactory<Item> provideItemFactory() {
        return id -> new Item(id);
    }

    @Provides
    Task getTask() {
        return item -> "ok"; // dummy getTask.
    }

    @Provides
    Stream<EventTrigger.Query> provideQueryStream() {
        //System.out.println("In provideQueryStream()");
        EventTrigger.Query<Item> query1 = new EventTriggerQuery<>("query1");
        query1.getPastSuccessfulEvents().add("Data_Received");
        EventTrigger.Query<Item> query2 = new EventTriggerQuery<>("query2");
        query2.getPastSuccessfulEvents().add("Data_Received");
        query2.getFutureEvents().add("Metadata_Archived");
        return Stream.of(query1, query2);
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
