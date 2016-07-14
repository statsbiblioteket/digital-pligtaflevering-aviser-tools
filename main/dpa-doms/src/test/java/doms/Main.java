package doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.bitrepository.LookupFileInfoForID;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsQuery;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Task;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.bitrepository.common.filestore.FileInfo;
import org.junit.Test;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
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
        //org.bitrepository.common.filestore.FileInfo fileInfo = new org.bitrepository.common.filestore.FileInfo();

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

        // get inputstream for an id in e.g. the bitrepository.
        Function<String, InputStream> inputStreamFor = id -> {
            try {
                return new LookupFileInfoForID().apply(id).getInputstream();
            } catch (IOException e) {
                throw new RuntimeException("could not get inputstream for " + id, e);
            }
        };


        QuerySpecification specification = new QuerySpecification(
                asList("Data_Received"),
                emptyList(),
                emptyList(),
                emptyList(),
                false
        );

        DomsQuery<Item> q = new DomsQuery<>(domsEventStorage, index);

        Stream<DomsItem> itemStream = q.query(specification);

        Task<DomsItem, String> task = item -> {
            DomsEvent event = new DomsEvent("agent", "details", item.toString(), false);
            item.events().add(event);
            item.datastreams().put("VERAPDF", "SAMPLE DATA");
            String bitrepositoryId = "";  // FIXME
            InputStream inputStream inputStreamFor.apply(bitrepositoryId);
            // FIXME
            
            return item.getOriginalItem().getDomsID() + " - " + item; // FIXME

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
