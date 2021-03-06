package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsItemTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FileNameToFileIDConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FilePathToChecksumPathConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.FileSystemDeliveryIngester;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.fedora.fedoraDBsearch.DBSearchRest;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.utils.AutoCloseablePutFileClient;
import dk.statsbiblioteket.newspaper.bitrepository.ingester.utils.BitrepositoryPutFileClientStub;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import io.vavr.control.Either;
import io.vavr.control.Try;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
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

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Event.STOPPED_STATE;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.CERTIFICATE_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.SETTINGS_DIR_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;

/**
 * Ingests the delivery in Doms and Bitrepo
 */
public class IngesterMain {

    public static final String DPA_DELIVERIES_FOLDER = "dpa.deliveries.folder";
    public static final String DPA_TEST_MODE = "dpa.testmode";
    public static final String DPA_PUTFILE_DESTINATION = "dpa.putfile.destinationpath";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.DaggerIngesterMain_DomsIngesterComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, IngesterModule.class, BitRepositoryModule.class})
    protected interface DomsIngesterComponent {

        Tool<String> getTool();
    }

    /**
     * @noinspection PointlessBooleanExpression
     */
    @Module
    public static class IngesterModule {

        Logger log = LoggerFactory.getLogger(this.getClass());

        /** @noinspection unchecked*/
        @Produces
        @Provides
        Tool<String> provideTool(@Named(DPA_DELIVERIES_FOLDER) String deliveriesFolder,
                         @Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         QuerySpecification workToDoQuery,
                         DomsRepository repository,
                         FileSystemDeliveryIngester ingester,
                         DefaultToolMXBean mxBean
        ) {
            final Path normalizedDeliveriesFolder = Paths.get(deliveriesFolder).normalize();
            final String agent = IngesterMain.class.getSimpleName();

            Tool<String> f = () -> {
                try (FileSystemDeliveryIngester autoCloseableIngester = ingester) { // shut down bitrepository resources completely when done
                    List<String> toolResults = Stream.of(workToDoQuery)
                            .flatMap(repository::query)
                            .peek(domsItem -> log.trace("Processing: {}", domsItem))
                            .peek(domsItem -> mxBean.currentId = domsItem.toString())
                            .map(DomsItemTuple::create)
                            .map(c -> c.map(domsItem -> {
                                try {
                                    final Either<Exception, ToolResult> result = ingester.apply(domsItem, normalizedDeliveriesFolder);
                                    return result;
                                } catch (Exception e) {
                                    return Either.left(e);
                                }
                            }))
                            .peek(c -> {
                                // Set events on the delivery top item.
                                final DomsItem item = Objects.requireNonNull(c.left());
                                if (c.right().isLeft()) {
                                    // Processing of _this_ domsItem threw unexpected exception
                                    item.appendEvent(new DomsEvent(agent, new Date(), DomsItemTuple.stacktraceFor(c.right().getLeft()), eventName, false));
                                } else {
                                    final ToolResult toolResult = ((Either<Exception, ToolResult>) c.right()).get(); // FIXME: Logic broken
                                    item.appendEvent(new DomsEvent(agent, new Date(), toolResult.getHumanlyReadableMessage(), eventName, toolResult.isSuccess()));
                                    if (toolResult.isSuccess() == false) {
                                        item.appendEvent(new DomsEvent(agent, new Date(), "autonomous component failed", STOPPED_STATE, true));
                                    }
                                }
                            })
                            .map(c -> c.left().getDomsId().id())
                            .collect(Collectors.toList());

                    return toolResults;
                }
            };
            return f;
        }

        /**
         * This is the folder have been put so we can locate the files corresponding to the trigger.
         *
         * @param map configuration map
         * @return
         */
        @Provides
        @Produces
        @Named(DPA_DELIVERIES_FOLDER)
        String provideDeliveriesFolder(ConfigurationMap map) {
            return map.getRequired(DPA_DELIVERIES_FOLDER);
        }

        /**
         * The path where PutfileClientStub places files This path is only used by the stub and not the real
         * bitrepositoryIngester
         *
         * @param map
         * @return
         */
        @Provides
        @Produces
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
        @Produces
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
        @Produces
        @Named(FileSystemDeliveryIngester.BITREPOSITORY_INGESTER_COLLECTIONID)
        String provideIngesterId(ConfigurationMap map) {
            return map.getRequired(FileSystemDeliveryIngester.BITREPOSITORY_INGESTER_COLLECTIONID);
        }

        /**
         * 'base' path to where batch/deliverable can be found by bitrepositoryClient
         *
         * @param map
         * @return
         */
        @Provides
        @Produces
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
        @Produces
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
        @Produces
        @Provides
        @Named(IngesterConfiguration.CERTIFICATE_PROPERTY)
        String provideCertificateProperty(ConfigurationMap map) {
            return map.getRequired(IngesterConfiguration.CERTIFICATE_PROPERTY);
        }

        @Produces
        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }

        /**
         * returns a comma-separated set of filenames (without path) to ignore when traversing the file tree. Naming is
         * kept the same as in Avisprojektet to keep configuration files similar
         *
         * @param map configuration map
         * @return string with comma separated file names.
         */
        @Produces
        @Provides
        @Named(ITERATOR_FILESYSTEM_IGNOREDFILES)
        String provideFilesystemIgnoredFiles(ConfigurationMap map) {
            return map.getRequired(ITERATOR_FILESYSTEM_IGNOREDFILES);
        }

        @Produces
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
         * @param dpaIngesterId       The ID of the collection, the ID has to match the id of the collection created in
         *                            fedora
         * @param destination         The destination where the client places the files
         * @param settingDir          The folder where settings for bitrepositoryClient is placed
         * @param certificateProperty The name of the certificate-file bor bitrepositoryClient
         * @return PutFileClient
         */
        @Produces
        @Provides
        AutoCloseablePutFileClient providePutFileClient(@Named(DPA_TEST_MODE) String testMode,
                                                        @Named(FileSystemDeliveryIngester.BITREPOSITORY_INGESTER_COLLECTIONID) String dpaIngesterId,
                                                        @Named(DPA_PUTFILE_DESTINATION) String destination,
                                                        @Named(SETTINGS_DIR_PROPERTY) String settingDir,
                                                        @Named(CERTIFICATE_PROPERTY) String certificateProperty,
                                                        Settings settings) {

            final AutoCloseablePutFileClient putClient;
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
                final ModifyComponentFactory factory = ModifyComponentFactory.getInstance();
                PutFileClient wrappedPutClient = factory.retrievePutClient(settings, securityManager, dpaIngesterId);
                putClient = new AutoCloseablePutFileClient() {
                    boolean closed = false;

                    @Override
                    public void close() throws Exception {
                        // https://github.com/bitrepository/reference/tree/master/bitrepository-client#closing-after-finishing
                        MessageBus messageBus = MessageBusManager.getMessageBus();
                        if (messageBus != null) {
                            log.trace("Closing wrappedPutFileClient {}", wrappedPutClient);
                            messageBus.close();
                        }
                        closed = true;
                    }

                    @Override
                    public void putFile(String collectionID, URL url, String fileID, long sizeOfFile, ChecksumDataForFileTYPE checksumForValidationAtPillar, ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation) {
                        if (closed == true) {
                            throw new RuntimeException("putFile() called after close()");
                        }
                        wrappedPutClient.putFile(collectionID, url, fileID, sizeOfFile, checksumForValidationAtPillar, checksumRequestsForValidation, eventHandler, auditTrailInformation);
                    }
                };
            }
            return putClient;
        }

        /**
         * Provide settings for PutfileClient and for EventHandler listening for the events from ingesting
         *
         * @param dpaIngesterId The ID of the collection
         * @param settingDir    The directory where the collection is located seen from the putFileClient
         * @return Settings for putfile PutfileClient and EventHandler
         */
        @Produces
        @Provides
        Settings provideSettings(@Named(FileSystemDeliveryIngester.BITREPOSITORY_INGESTER_COLLECTIONID) String
                                         dpaIngesterId,
                                 @Named(SETTINGS_DIR_PROPERTY) String settingDir) {
            SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(settingDir), dpaIngesterId);
            final Settings settings = settingsLoader.getSettings();
            return settings;
        }

        /**
         * Provide Function for converting filePath into an ID which is suitable for bitRepository
         *
         * @return and ID for the fileContent
         */
        @Produces
        @Provides
        FileNameToFileIDConverter provideFileNameToFileIDConverter() {
            //For creating flat filestructure for ingested files
            //return path -> NewspaperFileNameTranslater.getFileID(path.toString());

            //For creating folders for ingested files
            return path -> path.toString();
        }

        /**
         * Provide Function for converting filePath to the path relative to where the delivery is located
         *
         * @return and ID for the fileContent
         */
        @Produces
        @Provides
        public FilePathToChecksumPathConverter provideFilePathConverter() {
            //This should be used for the checksums in the specifications
            return (path1, deliveryFolderName) -> Paths.get(deliveryFolderName).relativize(path1).toString();
        }

        @Produces
        @Provides
        public Function<Path, Stream<Path>> provideDeliveriesForAbsolutePath() {
            return absolutePath -> Try.of(()
                    -> Files.walk(absolutePath, 1, FileVisitOption.FOLLOW_LINKS)
                    .filter(Files::isRegularFile)
                    .sorted()
            ).get();
        }
    }
}
