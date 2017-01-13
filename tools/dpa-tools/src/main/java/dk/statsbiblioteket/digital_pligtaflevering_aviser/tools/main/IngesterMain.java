package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FileNameToFileIDConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FilePathToChecksumPathConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.FileSystemDeliveryIngester;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.fedora.fedoraDBsearch.DBSearchRest;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.NewspaperFileNameTranslater;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.utils.BitrepositoryPutFileClientStub;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
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

import javax.inject.Named;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.CERTIFICATE_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.SETTINGS_DIR_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;

/**
 * Unfinished
 */
public class IngesterMain {

    public static final String DPA_DELIVERIES_FOLDER = "dpa.deliveries.folder";
    public static final String DPA_TEST_MODE = "dpa.testmode";
    public static final String DPA_PUTFILE_DESTINATION = "dpa.putfile.destinationpath";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerIngesterMain_DomsIngesterComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, IngesterModule.class, BitRepositoryModule.class})
    protected interface DomsIngesterComponent {
        Tool getTool();
    }

    @Module
    protected static class IngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(DPA_DELIVERIES_FOLDER) String deliveriesFolder,
                         QuerySpecification query,
                         DomsRepository repository,
                         FileSystemDeliveryIngester ingester
        ) {

            return () -> {
                final Path normalizedDeliveriesFolder = Paths.get(deliveriesFolder).normalize();

                List<String> toolResults = repository.query(query)
                        .map(domsItem -> ingester.apply(domsItem, normalizedDeliveriesFolder))
                        .collect(Collectors.toList());


                return String.valueOf(toolResults); // FIXME: Formalize output
            };
        }

        /**
         * This is the folder have been put so we can locate the files corresponding to the trigger.
         *
         * @param map configuration map
         * @return
         */
        @Provides
        @Named(DPA_DELIVERIES_FOLDER)
        String provideDeliveriesFolder(ConfigurationMap map) {
            return map.getRequired(DPA_DELIVERIES_FOLDER);
        }

        /**
         * The path where PutfileClientStub places files
         * This path is only used by the stub and not the real bitrepositoryIngester
         *
         * @param map
         * @return
         */
        @Provides
        @Named(DPA_PUTFILE_DESTINATION)
        String provideDestinationPath(ConfigurationMap map) {
            return map.getRequired(DPA_PUTFILE_DESTINATION);
        }

        /**
         * Indicates if a teststub simulating the putfileClient is used
         *
         * @param map
         * @return
         */
        @Provides
        @Named(DPA_TEST_MODE)
        String provideTestMode(ConfigurationMap map) {
            return map.getRequired(DPA_TEST_MODE);
        }

        /**
         * The ID of this collection, the ID has to match the id of the collection created in fedora
         *
         * @param map
         * @return
         */
        @Provides
        @Named(FileSystemDeliveryIngester.COLLECTIONID_PROPERTY)
        String provideIngesterId(ConfigurationMap map) {
            return map.getRequired(FileSystemDeliveryIngester.COLLECTIONID_PROPERTY);
        }



        /**
         * 'base' path to where batch/deliverable can be found by bitrepositoryClient
         *
         * @param map
         * @return
         */
        @Provides
        @Named(URL_TO_BATCH_DIR_PROPERTY)
        String provideUrlToBetrepositorysBatchPath(ConfigurationMap map) {
            return map.getRequired(URL_TO_BATCH_DIR_PROPERTY);
        }

        /**
         * The path to the directory where settings for putfileClient is placed
         *
         * @param map
         * @return
         */
        @Provides
        @Named(IngesterConfiguration.SETTINGS_DIR_PROPERTY)
        String provideSettingsProperty(ConfigurationMap map) {
            return map.getRequired(IngesterConfiguration.SETTINGS_DIR_PROPERTY);
        }

        /**
         * The name of the certificate used by putfileClient
         *
         * @param map
         * @return
         */
        @Provides
        @Named(IngesterConfiguration.CERTIFICATE_PROPERTY)
        String provideCertificateProperty(ConfigurationMap map) {
            return map.getRequired(IngesterConfiguration.CERTIFICATE_PROPERTY);
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

        @Provides
        @Named("pageSize")
        Integer providePageSize(ConfigurationMap map) {
            return Integer.valueOf(map.getRequired("pageSize"));
        }

        /**
         * returns a comma-separated set of filenames (without path) to ignore when traversing the file tree.
         * Naming is kept the same as in Avisprojektet to keep configuration files similar
         *
         * @param map configuration map
         * @return string with comma separated file names.
         */
        @Provides
        @Named(ITERATOR_FILESYSTEM_IGNOREDFILES)
        String provideFilesystemIgnoredFiles(ConfigurationMap map) {
            return map.getRequired(ITERATOR_FILESYSTEM_IGNOREDFILES);
        }

        @Provides
        DBSearchRest provideDBSearchRest(@Named(DOMS_URL) String domsUrl,
                                         @Named(DOMS_USERNAME) String domsUsername,
                                         @Named(DOMS_PASSWORD) String domsPassword) {
            try {
                return new DBSearchRest(new Credentials(domsUsername, domsPassword), domsUrl);
            } catch (MalformedURLException e) {
                throw new RuntimeException("domsUsername: " + domsUsername + " domsUrl: " + domsUrl, e);
            }
        }

        /**
         * Provides PutClient for interfacing to bitrepository
         *
         * @param testMode            If true a testclient is returned
         * @param dpaIngesterId       The ID of the collection, the ID has to match the id of the collection created in fedora
         * @param destination         The destination where the client places the files
         * @param settingDir          The folder where settings for bitrepositoryClient is placed
         * @param certificateProperty The name of the certificate-file bor bitrepositoryClient
         * @return PutFileClient
         */
        @Provides
        PutFileClient providePutFileClient(@Named(DPA_TEST_MODE) String testMode,
                                           @Named(FileSystemDeliveryIngester.COLLECTIONID_PROPERTY) String dpaIngesterId,
                                           @Named(DPA_PUTFILE_DESTINATION) String destination,
                                           @Named(SETTINGS_DIR_PROPERTY) String settingDir,
                                           @Named(CERTIFICATE_PROPERTY) String certificateProperty,
                                           Settings settings) {

            BitrepositoryPutFileClientStub putClient;
            if (Boolean.parseBoolean(testMode)) {
                putClient = new BitrepositoryPutFileClientStub(destination);
            } else {
                //This is for Authentication for bitrepository-client, This is copied directly from BitrepositoryIngesterComponent.createPutFileClient in dpa-bitrerepository-ingester
                String certificateLocation = settingDir + File.separator + certificateProperty;
                PermissionStore permissionStore = new PermissionStore();
                MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
                MessageSigner signer = new BasicMessageSigner();
                OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
                org.bitrepository.protocol.security.SecurityManager securityManager = new BasicSecurityManager(settings.getRepositorySettings(), certificateLocation, authenticator, signer, authorizer, permissionStore, dpaIngesterId);
                return ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, dpaIngesterId);
            }
            return putClient;
        }

        /**
         * Provide settings for PutfileClient and for EventHandler listening for the events from ingesting
         * @param dpaIngesterId The ID of the collection
         * @param settingDir The directory where the collection is located seen from the putFileClient
         * @return Settings for putfile PutfileClient and EventHandler
         */
        @Provides
        Settings provideSettings(@Named(FileSystemDeliveryIngester.COLLECTIONID_PROPERTY) String dpaIngesterId,
                                     @Named(SETTINGS_DIR_PROPERTY) String settingDir) {
            SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(settingDir), dpaIngesterId);
            return settingsLoader.getSettings();
        }

        /**
         * Provide Function for converting filePath into an ID which is suitable for bitRepository
         * @return and ID for the fileContent
         */
        @Provides
        FileNameToFileIDConverter provideFileNameToFileIDConverter() {
            return path1 -> NewspaperFileNameTranslater.getFileID(path1.toString());
        }

        /**
         * Provide Function for converting filePath a for
         * @return and ID for the fileContent
         */
        @Provides
        public FilePathToChecksumPathConverter provideFilePathConverter() {
            //This should be used for the checksums in the deliveries from Infomedia
            return (path1, batchId) -> path1.getFileName().toString();

            //This should be used for the checksums in the specifications
            //return (path1, batchId) -> Paths.get(path1.toString(), batchId).relativize(path1).toString();
        }

    }
}
