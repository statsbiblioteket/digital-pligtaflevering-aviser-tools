package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.ParallelOperationLimiter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.TreeIngester;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the directory structure of a batch. This should run both at Ninestars and at SB.
 */
public class BitrepositoryIngesterComponent extends TreeProcessorAbstractRunnableComponent {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public BitrepositoryIngesterComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getEventID() {
        return "Data_Archived";
    }

    /**
     * Ingests all the jp2 files for the indicated batch into the configured bit repository the indicated batch.
     */
    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws Exception {
        IngesterConfiguration configuration = new IngesterConfiguration(getProperties());
        Settings settings = loadSettings(configuration);

        if(!forceOnline(batch, configuration)) {
            resultCollector.addFailure(batch.getFullID(), "ingest", getClass().getSimpleName(), 
                    "Failed to force batch online. Skipping ingest of batch");
            return;
        }
        
        ParallelOperationLimiter parallelOperationLimiter = new ParallelOperationLimiter(configuration.getMaxNumberOfParallelPuts());
        PutFileClient ingestClient = createPutFileClient(configuration, settings);

        try (DomsJP2FileUrlRegister urlRegister = new DomsJP2FileUrlRegister(batch, 
                                                                            createEnhancedFedora(configuration), 
                                                                            configuration.getBitmagBaseUrl(), 
                                                                            resultCollector, 
                                                                            configuration.getMaxThreads(), 
                                                                            configuration.getDomsTimeout());
             TreeIngester ingester = new TreeIngester(configuration.getCollectionID(), 
                                                     parallelOperationLimiter,
                                                     urlRegister, 
                                                     new BatchImageLocator(createIterator(batch), configuration.getUrlToBatchDir()), 
                                                     ingestClient, 
                                                     resultCollector,
                                                     configuration.getMaxBitmagRetries(), batch)) {
      
            log.info("Starting ingest of batch '" + batch.getFullID() + "'");
            ingester.performIngest();
        }

        log.info("Finished ingest of batch '" + batch.getFullID() + "'");
    }

    /**
     * Method to handle the task of forcing (keeping) files online when they are ingested. 
     * The method calls a command that's present on PATH. 
     * @param batch The batch from which to keep files online 
     * @param ingesterConfiguration the configuration (for figuring out which command to call)
     */
    private boolean forceOnline(Batch batch, IngesterConfiguration ingesterConfiguration) throws IOException {
        boolean success = false;
        String forceOnlineCommand = ingesterConfiguration.getForceOnlineCommand();
        List<String> command = new ArrayList<String>();
        command.add(forceOnlineCommand);
        command.add(batch.getFullID());

        int exitCode = -1;
        try {
            Process forceOnlineProcess = new ProcessBuilder(command).start();
            exitCode = forceOnlineProcess.waitFor();
            if(exitCode == 0) {
                success = true;
            } else {
                log.warn("Call to forceOnline command was not a success. Command was: '" + command.toString() + "'");
                success = false;
            } 
        } catch (InterruptedException e) {
            log.error("Was interrupted while calling forceOnline command. Command was: '" + command.toString() + "'.");
        }

        return success;
    }

    protected EnhancedFedora createEnhancedFedora(IngesterConfiguration ingesterConfig) {
        Credentials creds = new Credentials(ingesterConfig.getDomsUser(), ingesterConfig.getDomsPass());
        try {
            EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                    creds,
                    ingesterConfig.getDomsUrl(),
                    ingesterConfig.getPidgeneratorurl(), null,
                    ingesterConfig.getFedoraRetries(),
                    ingesterConfig.getDelayBetweenFedoraRetries()
                    );
            return fedora;
        } catch (MalformedURLException | PIDGeneratorException | JAXBException e) {
            throw new RuntimeException("Failed to get a connection to DOMS.", e);
        }
    }

    /**
     * Creates a default put file client. May be overridden by specialized BitrepositoryIngesterComponents.
     */
    protected PutFileClient createPutFileClient(IngesterConfiguration configuration, Settings settings) {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        org.bitrepository.protocol.security.SecurityManager securityManager =
                new BasicSecurityManager(settings.getRepositorySettings(),
                        configuration.getCertificateLocation(),
                        authenticator, signer, authorizer, permissionStore, configuration.getComponentID());
        return ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, 
                configuration.getComponentID());
    }

    /**
     * Load settings from disk. May be overridden by specialized custom functionality.
     */
    protected Settings loadSettings(IngesterConfiguration configuration) {
        SettingsProvider settingsLoader = new SettingsProvider(
                new XMLFileSettingsLoader(configuration.getSettingsDir()),
                configuration.getComponentID());
        return settingsLoader.getSettings();
    }
}
