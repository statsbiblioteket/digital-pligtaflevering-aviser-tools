package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.EventQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex_DigitalPligtafleveringAviser;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;

import javax.inject.Named;
import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_FUTURE_EVENTS;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_ITEM_TYPES;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_OLD_EVENTS;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_PAST_SUCCESSFUL_EVENTS;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_COLLECTION;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.FEDORA_DELAY_BETWEEN_RETRIES;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.FEDORA_RETRIES;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.SBOI_PAGESIZE;
import static java.util.Arrays.asList;
import javax.enterprise.inject.Produces;

/**
 * DOMS configuration string lookup providers.
 */
@Module
public class DomsModule {

    
    /**
     * URL for accessing the DOMS repository. No default value.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(DOMS_URL)
    public String provideDomsURL(ConfigurationMap map) {
        return map.getRequired(DOMS_URL);
    }

    /**
     * Username for accessing DOMS. No default.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(DOMS_USERNAME)
    public String provideDomsUserName(ConfigurationMap map) {
        return map.getRequired(DOMS_USERNAME);
    }

    /**
     * URL for DOMS pid generator. No default
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(DOMS_PIDGENERATOR_URL)
    public String provideDomsPidGeneratorURL(ConfigurationMap map) {
        return map.getRequired(DOMS_PIDGENERATOR_URL);
    }

    /**
     * Patht to the folder containing batches
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(ITERATOR_FILESYSTEM_BATCHES_FOLDER)
    public String provideBatcFolderPath(ConfigurationMap map) {
        return map.getRequired(ITERATOR_FILESYSTEM_BATCHES_FOLDER);
    }

    /**
     * Password for accessing DOMS. No default.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(DOMS_PASSWORD)
    public String provideDomsPassword(ConfigurationMap map) {
        return map.getRequired(DOMS_PASSWORD);
    }

    /**
     * URL for accessing SBOI. No default
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(AUTONOMOUS_SBOI_URL)
    public String provideSummaLocation(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_SBOI_URL);
    }

    /**
     * Number of retries for retrieving data from DOMS Fedora before giving up.
     */
    @Produces
    @Provides
    @Named(FEDORA_RETRIES)
    public int getFedoraRetries(ConfigurationMap map) {
        return map.getRequiredInt(FEDORA_RETRIES);
    }

    /**
     * Delay between retries for retrieving data from DOMS Fedora before giving
     * up.
     */
    @Produces
    @Provides
    @Named(FEDORA_DELAY_BETWEEN_RETRIES)
    public int getFedoraDelayBetweenRetries(ConfigurationMap map) {
        return map.getRequiredInt(FEDORA_DELAY_BETWEEN_RETRIES);
    }

    /**
     * Create Modified_SBOIEventIndex from dependencies provided by Dagger so we
     * can search in the right collection.
     */
    @Produces
    @Provides
    public SBOIEventIndex<Item> provideSBOIEventIndex(
            @Named(AUTONOMOUS_SBOI_URL) String summaLocation,
            PremisManipulatorFactory<Item> premisManipulatorFactory,
            DomsEventStorage<Item> domsEventStorage,
            @Named(SBOI_PAGESIZE) int pageSize,
            @Named(DOMS_COLLECTION) String recordBase) {

        try {
            return new SBOIEventIndex_DigitalPligtafleveringAviser<Item>(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize, recordBase);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Provider to give PremisManipulatorFactory as we cannot modify the actual
     * constructor with the Dagger annotations.
     *
     * @param itemFactory factory to create new, empty items.
     * @return factory
     */
    @Produces
    @Provides
    public PremisManipulatorFactory<Item> providePremisManipulatorFactory(ItemFactory<Item> itemFactory) {
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
     * @param domsURL URL to contact DOMS
     * @param domsPidGeneratorURL URL to contact PID generator web service.
     * @param domsUserName DOMS user name
     * @param domsPassword DOMS password
     * @param itemFactory factory for new fresh items for JAXB to populate.
     * @return
     */
    @Produces
    @Provides
    public DomsEventStorage<Item> provideDomsEventStorage(
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

    @Produces
    @Provides
    public EnhancedFedora provideEnhancedFedora(
            @Named(DOMS_USERNAME) String domsUserName,
            @Named(DOMS_PASSWORD) String domsPassword,
            @Named(DOMS_URL) String fedoraLocation,
            @Named(DOMS_PIDGENERATOR_URL) String domsPidgeneratorUrl,
            @Named(FEDORA_RETRIES) int fedoraRetries,
            @Named(FEDORA_DELAY_BETWEEN_RETRIES) int fedoraDelayBetweenRetries) {
        Credentials creds = new Credentials(domsUserName, domsPassword);

        EnhancedFedoraImpl eFedora;
        try {
            eFedora = new EnhancedFedoraImpl(
                    creds,
                    fedoraLocation,
                    domsPidgeneratorUrl,
                    null, // FIXME:  What is this?  Goes in the description?
                    fedoraRetries,
                    fedoraDelayBetweenRetries);
        } catch (JAXBException | PIDGeneratorException
                | MalformedURLException e) {
            throw new RuntimeException("EnhancedFedoraImpl constructor failed");
        }
        return eFedora;
    }

    /**
     * Extracts a EventQuerySpecification from configuration parameters. The
     * semantics are the same as for the Newspaper Autonomous components.
     *
     * @param pastSuccessfulEvents Comma separated list of event names which
     * must have been completed successfully previously for item to be
     * considered. An empty string will include template objects which is
     * seldomly wanted.
     * @param futureEvents Comma separated list of events which must not have
     * happened yet regardless of their outcome.
     * @param oldEvents Comma separated list of events which 1) must never have
     * happened, or 2) have happened but a datastream (any datastream other than
     * EVENTS which holds the events) have been written to since that event
     * occured (the time stamp of the datastream is newer than the time stamp of
     * the event).
     * @param itemTypes Comma separated list of types which item MUST have to be
     * considered.
     * @return
     */
    @Produces
    @Provides
    public QuerySpecification providesWorkToDoQuerySpecification(
            @Named(AUTONOMOUS_PAST_SUCCESSFUL_EVENTS) String pastSuccessfulEvents,
            @Named(AUTONOMOUS_FUTURE_EVENTS) String futureEvents,
            @Named(AUTONOMOUS_OLD_EVENTS) String oldEvents,
            @Named(AUTONOMOUS_ITEM_TYPES) String itemTypes) {
        // http://stackoverflow.com/a/7488676/53897
        return new EventQuerySpecification(
                asList(pastSuccessfulEvents.split("\\s*,\\s*")),
                asList(futureEvents.split("\\s*,\\s*")),
                asList(oldEvents.split("\\s*,\\s*")),
                asList(itemTypes.split("\\s*,\\s*")), false);
    }

    /**
     * Comma separated list of events which must have been completed
     * successfully previously for item to be considered.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(AUTONOMOUS_PAST_SUCCESSFUL_EVENTS)
    public String providePastSuccesfulEvents(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_PAST_SUCCESSFUL_EVENTS);
    }

    /**
     * Comma separated list of events which may not yet have happened (FIXME:
     * Successfully?) for item to be considered.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Produces
    @Provides
    @Named(AUTONOMOUS_FUTURE_EVENTS)
    public String provideFutureEvents(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_FUTURE_EVENTS);
    }

    /**
     * Comma separated list of events which (FIXME: what exactly?) for item to
     * be considered.
     *
     * @param map configuration map containing the value.
     * @return
     */
    @Provides
    @Produces
    @Named(AUTONOMOUS_OLD_EVENTS)
    public String provideOldEvents(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_OLD_EVENTS);
    }

    /**
     * Comma separated list of types which item must have to be considered.
     *
     * @param map configuration map containing the value.
     * @return configuration value (mandatory)
     */
    @Provides
    @Produces
    @Named(AUTONOMOUS_ITEM_TYPES)
    public String provideItemTypes(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_ITEM_TYPES);
    }

    /**
     * This creates a configured WebResource which can talk to Fedora based on
     * the credentials given in the configuration map. For historical reasons
     * (meaning to be compatible with the existing configuration files) we do
     * the same silent adding on "/objects/" as was done in IteratorForFedora3
     * constructor in item-event-framework-common.
     *
     * @param domsUrl url to Fedora Commons instance
     * @param domsUsername username for Fedora Commons instance
     * @param domsPassword password for Fedora Commons instance
     * @return ready-to-use WebResource for Fedora Commons interaction.
     */
    @Provides
    @Produces
    @Named(DomsId.DPA_WEBRESOURCE) public WebResource provideConfiguredFedoraWebResource(
            @Named(DOMS_URL) String domsUrl,
            @Named(DOMS_USERNAME) String domsUsername,
            @Named(DOMS_PASSWORD) String domsPassword) {
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(domsUsername, domsPassword));
        WebResource webResource = client.resource(domsUrl.endsWith("/objects/") ? domsUrl : domsUrl + "/objects/");
        return webResource;
    }

    /**
     * Return the number of responses in a single sboi query
     */
    @Provides
    @Produces
    @Named(SBOI_PAGESIZE)
    public Integer providePageSize(ConfigurationMap map) {
        return Integer.valueOf(map.getRequired(SBOI_PAGESIZE));
    }

    @Provides
    @Produces
    @Named(DOMS_COLLECTION)
    public String provideDomsCollection(ConfigurationMap map) {
        return map.getRequired(DOMS_COLLECTION);
    }

}
