package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters.FileSystemIngester;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.doms.central.connectors.fedora.fedoraDBsearch.DBSearchRest;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration;
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

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ingesters.FileSystemIngester.DPA_PUTFILE_DESTINATION;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.CERTIFICATE_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.SETTINGS_DIR_PROPERTY;

/**
 * Unfinished
 */
public class IngesterMain {

    public static final String DPA_DELIVERIES_FOLDER = "dpa.deliveries.folder";

    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerIngesterMain_DomsIngesterComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, IngesterModule.class})
    protected interface DomsIngesterComponent {
        Tool getTool();
    }

    @Module
    protected static class IngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(DPA_DELIVERIES_FOLDER) String deliveriesFolder,
                         @Named(DPA_PUTFILE_DESTINATION) String dpaPutfileDestination,
                         @Named(FileSystemIngester.DPA_TEST_MODE) String dpaTestmode,
                         @Named(FileSystemIngester.COLLECTIONID_PROPERTY) String collectionId,
                         QuerySpecification query,
                         DomsRepository repository,
                         FileSystemIngester ingester
        ) {

            return () -> {
                final Path normalizedDeliveriesFolder = Paths.get(deliveriesFolder).normalize();
                List<String> result = repository.query(query)
                        .map(id -> ingester.apply(id, normalizedDeliveriesFolder))
                        .collect(Collectors.toList());

                return String.valueOf(result); // FIXME: Formalize output
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

        //TODO:BEFORE COMMIT DPA-59 MAKE SURE
        @Provides
        @Named(DPA_PUTFILE_DESTINATION)
        String provideDestinationPath(ConfigurationMap map) {
            return map.getRequired(DPA_PUTFILE_DESTINATION);
        }

        @Provides
        @Named(FileSystemIngester.DPA_TEST_MODE)
        String provideTestMode(ConfigurationMap map) {
            return map.getRequired(FileSystemIngester.DPA_TEST_MODE);
        }

        @Provides
        @Named(FileSystemIngester.COLLECTIONID_PROPERTY)
        String provideIngesterId(ConfigurationMap map) {
            return map.getRequired(FileSystemIngester.COLLECTIONID_PROPERTY);
        }


        @Provides
        @Named(IngesterConfiguration.SETTINGS_DIR_PROPERTY)
        String a1(ConfigurationMap map) {
            return map.getRequired(IngesterConfiguration.SETTINGS_DIR_PROPERTY);
        }

        @Provides
        @Named(IngesterConfiguration.CERTIFICATE_PROPERTY)
        String a2(ConfigurationMap map) {
            return map.getRequired(IngesterConfiguration.CERTIFICATE_PROPERTY);
        }

        @Provides
        @Named(BITMAG_BASEURL_PROPERTY)
        String provideBitmagUrl(ConfigurationMap map) {
            return map.getRequired(BITMAG_BASEURL_PROPERTY);
        }


        public static final String DOMS_TIMEOUT = "bitrepository.ingester.domsTimeout";

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


        @Provides
        PutFileClient provideDummyPutFileClient(@Named(FileSystemIngester.DPA_TEST_MODE) String testMode,
                                                @Named(FileSystemIngester.COLLECTIONID_PROPERTY) String dpaIngesterId,
                                                @Named(DPA_PUTFILE_DESTINATION) String destination,
                                                @Named(SETTINGS_DIR_PROPERTY) String settingDir,
                                                @Named(CERTIFICATE_PROPERTY) String certificateProperty) {

            BitrepositoryPutFileClientStub putClient;
            if(Boolean.parseBoolean(testMode)) {
                putClient = new BitrepositoryPutFileClientStub(destination);
            } else {

                String certificateLocation = settingDir + File.separator + certificateProperty;

                SettingsProvider settingsLoader = new SettingsProvider(
                        new XMLFileSettingsLoader(settingDir),
                        dpaIngesterId);

                Settings bitRepoSet = settingsLoader.getSettings();


                PermissionStore permissionStore = new PermissionStore();
                MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
                MessageSigner signer = new BasicMessageSigner();
                OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
                org.bitrepository.protocol.security.SecurityManager securityManager =
                        new BasicSecurityManager(bitRepoSet.getRepositorySettings(),
                                certificateLocation,
                                authenticator, signer, authorizer, permissionStore, dpaIngesterId);
                return ModifyComponentFactory.getInstance().retrievePutClient(bitRepoSet, securityManager,
                        dpaIngesterId);
            }
            return putClient;


        }

    }
}
