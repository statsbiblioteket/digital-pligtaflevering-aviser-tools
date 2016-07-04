package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.medieplatform.autonomous.*;

import java.util.Date;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello world - args = " + asList(args));

        ItemFactory<Item> itemFactory = id -> new Item(id);

        DomsEventStorageFactory<Item> domsEventStorageFactory = new DomsEventStorageFactory<>();
        domsEventStorageFactory.setFedoraLocation("http://localhost:7880/fedora");
        domsEventStorageFactory.setPidGeneratorLocation("http://localhost:7880/pidgenerator-service");
        //domsEventStorageFactory.setUsername(properties.getProperty(DOMS_USERNAME));
        //domsEventStorageFactory.setPassword(properties.getProperty(DOMS_PASSWORD));
        domsEventStorageFactory.setItemFactory(itemFactory);

        DomsEventStorage domsEventStorage = domsEventStorageFactory.createDomsEventStorage();
        SBOIEventIndex<Item> index = new SBOIEventIndex<>(
                "http://localhost:58608/newspapr/sbsolr/",
                new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE, itemFactory),
                domsEventStorage,
                10
        );

        QuerySpecification specification = new QuerySpecification(asList("Data_Received"), emptyList(), emptyList(), emptyList());
        DomsQuery<Item> q = new DomsQuery<>(index);

        q.query(specification).forEach(System.out::println);
//        index.search(false, query).forEachRemaining(System.out::println);
//        index.search(true, query).forEachRemaining(System.out::println);
//        index.search(true, query).forEachRemaining(item -> {
//            System.out.println(item);
//            Date d = appendEventToItem(domsEventStorage, (Item) item);
//            System.out.println(d);
//        });

        // Missing "add event"...

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
}
