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

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.FEDORA_DELAY_BETWEEN_RETRIES;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.FEDORA_RETRIES;

/**
 * DOMS configuration string lookup providers.
 */
@Module
public class DomsModule {
    /**
     * URL for accessing the DOMS repository.  No default value.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Provides
    @Named(DOMS_URL)
    String provideDomsURL(ConfigurationMap map) {
        return map.getRequired(DOMS_URL);
    }

    /**
     * Username for accessing DOMS.  No default.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Provides
    @Named(DOMS_USERNAME)
    String provideDomsUserName(ConfigurationMap map) {
        return map.getRequired(DOMS_USERNAME);
    }

    /**
     * URL for DOMS pid generator.  No default
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Provides
    @Named(DOMS_PIDGENERATOR_URL)
    String provideDomsPidGeneratorURL(ConfigurationMap map) {
        return map.getRequired(DOMS_PIDGENERATOR_URL);
    }

    /**
     * Password for accessing DOMS.  No default.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Provides
    @Named(DOMS_PASSWORD)
    String provideDomsPassword(ConfigurationMap map) {
        return map.getRequired(DOMS_PASSWORD);
    }

    /**
     * URL for accessing SBOI.  No default
     *
     * @param map configuration map containing the value.
     * @return
     */

    @Provides
    @Named(AUTONOMOUS_SBOI_URL)
    String provideSummaLocation(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_SBOI_URL);
    }

    /**
     * Number of retries for retrieving data from DOMS Fedora before giving up.
     */

    @Provides
    @Named(FEDORA_RETRIES)
    int getFedoraRetries(ConfigurationMap map) {
        return map.getRequiredInt(FEDORA_RETRIES);
    }

    /**
     * Delay between retries for retrieving data from DOMS Fedora before giving up.
     */

    @Provides
    @Named(FEDORA_DELAY_BETWEEN_RETRIES)
    int getFedoraDelayBetweenRetries(ConfigurationMap map) {
        return map.getRequiredInt(FEDORA_DELAY_BETWEEN_RETRIES);
    }

    /**
     * Create SBOIEventIndex from dependencies provided by Dagger.
     *
     * @param summaLocation            URL for summa
     * @param premisManipulatorFactory factory for premisManipulators
     * @param domsEventStorage         event storage
     * @param pageSize                 items to get from summa at a time.
     * @return
     */
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

    /**
     * Provider to give PremisManipulatorFactory as we cannot modify the actual constructor with the Dagger annotations.
     *
     * @param itemFactory factory to create new, empty items.
     * @return factory
     */
    @Provides
    PremisManipulatorFactory providePremisManipulatorFactory(ItemFactory<Item> itemFactory) {
        try {
            PremisManipulatorFactory<Item> factory;
            factory = new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE, itemFactory);
            return factory;
        } catch (JAXBException e) {
            throw new RuntimeException("new PremisManipulatorFactory()", e);
        }
    }

    /**
     * Create DomsEventStorage factory using appropriate
     *
     * @param domsURL             URL to contact DOMS
     * @param domsPidGeneratorURL URL to contact PID generator web service.
     * @param domsUserName        DOMS user name
     * @param domsPassword        DOMS password
     * @param itemFactory         factory for new fresh items for JAXB to populate.
     * @return
     */
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
        try {
            DomsEventStorage<Item> domsEventStorage = domsEventStorageFactory.createDomsEventStorage();
            return domsEventStorage;
        } catch (JAXBException | PIDGeneratorException | MalformedURLException e) {
            throw new RuntimeException("createDomsEventStorage()", e);
        }
    }

}
