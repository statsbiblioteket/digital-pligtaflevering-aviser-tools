package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;

import javax.inject.Named;
import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.Objects;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;

/**
 *
 */
@Module
public class DomsModule {
    @Provides
    @Named(DOMS_URL)
    String provideDomsURL(ConfigurationMap map) {
        return Objects.requireNonNull(map.get(DOMS_URL), DOMS_URL);
    }

    @Provides
    @Named(DOMS_USERNAME)
    String provideDomsUserName(ConfigurationMap map) {
        return Objects.requireNonNull(map.get(DOMS_USERNAME), DOMS_USERNAME);
    }

    @Provides
    @Named(DOMS_PIDGENERATOR_URL)
    String provideDomsPidGeneratorURL(ConfigurationMap map) {
        return Objects.requireNonNull(map.get(DOMS_PIDGENERATOR_URL), DOMS_PIDGENERATOR_URL);
    }

    @Provides
    @Named(DOMS_PASSWORD)
    String provideDomsPassword(ConfigurationMap map) {
        return Objects.requireNonNull(map.get(DOMS_PASSWORD), DOMS_PASSWORD);
    }

    @Provides
    @Named(AUTONOMOUS_SBOI_URL)
    String provideSummaLocation(ConfigurationMap map) {
        return Objects.requireNonNull(map.get(AUTONOMOUS_SBOI_URL), AUTONOMOUS_SBOI_URL);
    }

    @Provides
    SBOIEventIndex provideSBOIEventIndex(@Named(AUTONOMOUS_SBOI_URL) String summaLocation,
                                         PremisManipulatorFactory premisManipulatorFactory,
                                         DomsEventStorage<Item> domsEventStorage,
                                         @Named("pageSize") int pageSize) {
        try {
            SBOIEventIndex sboiEventIndex = new SBOIEventIndex(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize);
            return sboiEventIndex;
        } catch (MalformedURLException e) {
            throw new RuntimeException("new SBOIEventIndex(...)", e);
        }
    }

    @Provides
    PremisManipulatorFactory providePremisManipulatorFactory(ItemFactory<Item> itemFactory) {
        try {
            PremisManipulatorFactory<Item> premisManipulatorFactory;
            premisManipulatorFactory = new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE, itemFactory);
            return premisManipulatorFactory;
        } catch (JAXBException e) {
            throw new RuntimeException("new PremisManipulatorFactory()", e);
        }
    }

    @Provides
    DomsEventStorage<Item> provideDomsEventStorage(
            @Named(DOMS_URL) String domsURL,
            @Named(DOMS_PIDGENERATOR_URL) String domsPidGeneratorURL,
            @Named(DOMS_USERNAME) String domsUserName,
            @Named(DOMS_PASSWORD) String domsPassword,
            ItemFactory<Item> itemFactory) {
        DomsEventStorageFactory<Item> domsEventStorageFactory = new DomsEventStorageFactory<>();
        domsEventStorageFactory.setFedoraLocation(domsURL);
        domsEventStorageFactory.setPidGeneratorLocation(domsPidGeneratorURL);
        domsEventStorageFactory.setUsername(domsUserName);
        domsEventStorageFactory.setPassword(domsPassword);
        domsEventStorageFactory.setItemFactory(itemFactory);
        DomsEventStorage<Item> domsEventStorage = null;
        try {
            domsEventStorage = domsEventStorageFactory.createDomsEventStorage();
        } catch (JAXBException | PIDGeneratorException | MalformedURLException e) {
            throw new RuntimeException("createDomsEventStorage()", e);
        }
        return domsEventStorage;
    }

}
