package doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.*;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Task;
import dk.statsbiblioteket.medieplatform.autonomous.*;
import org.junit.Test;

import javax.inject.Provider;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception {
    }

    @Test
    public void Main() throws Exception {
        ItemFactory<Item> itemFactory = id -> new Item(id);

        final Provider<DomsEventStorage<Item>> domsEventStorageProvider = getDomsEventStorageProvider(
                itemFactory,
                "http://localhost:7880/fedora",
                "http://localhost:7880/pidgenerator-service");

        final DomsEventStorage<Item> domsEventStorage = domsEventStorageProvider.get();

        SBOIEventIndex<Item> index = new SBOIEventIndex<>(
                "http://localhost:58608/newspapr/sbsolr/",
                new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE, itemFactory),
                domsEventStorage,
                10
        );

        QuerySpecification specification = new QuerySpecification(
                asList("Data_Received"),
                emptyList(),
                emptyList(),
                emptyList(),
                false
        );

        DomsQuery<Item> q = new DomsQuery<>(domsEventStorage, index);

        Stream<DomsEventAdder> itemStream = q.query(specification);

        Task<DomsEventAdder, String> task = item -> {
            DomsEvent event = new DomsEvent("agent", "details", item.toString(), false);
            item.add(event);

            return item.toString();  // FIXME
        };

        List<String> result = itemStream.map(task).collect(Collectors.toList());

        System.out.println(result); // FIXME
    }

    public Provider<DomsEventStorage<Item>> getDomsEventStorageProvider(ItemFactory<Item> itemFactory, String
            fedoraLocation, String pidGeneratorLocation) {
        DomsEventStorageFactory<Item> domsEventStorageFactory = new DomsEventStorageFactory<>();
        domsEventStorageFactory.setFedoraLocation(fedoraLocation);
        domsEventStorageFactory.setPidGeneratorLocation(pidGeneratorLocation);
        //domsEventStorageFactory.setUsername(properties.getProperty(DOMS_USERNAME));
        //domsEventStorageFactory.setPassword(properties.getProperty(DOMS_PASSWORD));
        domsEventStorageFactory.setItemFactory(itemFactory);

        return () -> {
            try {
                return domsEventStorageFactory.createDomsEventStorage();
            } catch (Exception e) {
                throw new RuntimeException("get() failed", e);
            }
        };
    }
}
