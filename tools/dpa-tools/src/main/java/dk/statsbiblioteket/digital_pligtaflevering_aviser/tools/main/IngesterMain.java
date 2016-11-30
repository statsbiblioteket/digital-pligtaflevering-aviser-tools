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
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_IGNOREDFILES;

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

    }
}
