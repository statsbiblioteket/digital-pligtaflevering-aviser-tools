package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_DELIVERIES_FOLDER;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.IngesterMain.DPA_PUTFILE_DESTINATION;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.SETTINGS_DIR_PROPERTY;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;

/**
 * Note:  May require a lot of memory.
 */
public class IngesterMainTest {
    @Test
    public void ingestAllTestBatches() throws URISyntaxException {
        String batchDirPathInWorkspace = "delivery-samples";
        String bitrepoDirPathInWorkspace = "bitrepositorystub-storage";

        // http://stackoverflow.com/a/320595/53897
        URI l = IngesterMainTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.

        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        Path bitrepoPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, bitrepoDirPathInWorkspace);

        IngesterMain.main(new String[]{
                "ingester.properties",
                DPA_DELIVERIES_FOLDER + "=" + batchPath.toAbsolutePath(),
                BITREPOSITORY_SBPILLAR_MOUNTPOINT + "=" + bitrepoPath.toAbsolutePath(),
                BITMAG_BASEURL_PROPERTY + "=http://localhost:58709/var/reference1pillar/dpaviser/fileDir/",
                SETTINGS_DIR_PROPERTY + "=" + startDir.toAbsolutePath(),
                URL_TO_BATCH_DIR_PROPERTY + "=file://" + batchPath.toAbsolutePath() + "/",
                DPA_PUTFILE_DESTINATION+ "=" + bitrepoPath.toAbsolutePath() + "/var/reference1pillar/dpaviser/fileDir",
                "pageSize=9999"
        });
    }
}
