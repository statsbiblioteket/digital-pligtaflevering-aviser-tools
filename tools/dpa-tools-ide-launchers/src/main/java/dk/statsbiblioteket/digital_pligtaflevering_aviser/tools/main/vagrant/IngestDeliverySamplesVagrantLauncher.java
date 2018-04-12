package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.vagrant;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.SETTINGS_DIR_PROPERTY;

/**
 * Launcher suitable for invoking IngesterMain from within an IDE using the delivery-samples folder.
 *
 */
public class IngestDeliverySamplesVagrantLauncher {

    public static void main(String[] args) throws Exception {

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(IngestDeliverySamplesVagrantLauncher.class, "delivery-samples");
        Path bitrepoPath = MavenProjectsHelper.getRequiredPathTowardsRoot(IngestDeliverySamplesVagrantLauncher.class, "bitrepositorystub-storage");

        // http://stackoverflow.com/a/320595/53897
        URI l = IngestDeliverySamplesVagrantLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        IngesterMain.main(new String[]{
                "vagrant/ingester.properties",
                IngesterMain.DPA_DELIVERIES_FOLDER + "=" + batchPath.toAbsolutePath(),
                BITREPOSITORY_SBPILLAR_MOUNTPOINT + "=" + bitrepoPath.toAbsolutePath(),
                BITMAG_BASEURL_PROPERTY + "=http://vagrant:58709/var/file1pillar/files/dpaviser/folderDir/",
                SETTINGS_DIR_PROPERTY + "=" + startDir.toAbsolutePath(),  // where "resources" end up compiled.
        });
    }
}
