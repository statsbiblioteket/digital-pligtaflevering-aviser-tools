package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;

/**
 *
 */

public class VeraPDFInvokeMainTest {
    @org.junit.Test
    public void invocationTest() throws URISyntaxException {

        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(this, "delivery-samples");
        Path bitrepoPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(this, "bitrepositorystub-storage");

        VeraPDFInvokeMain.main(new String[]{
                "verapdf-invoke-vagrant.properties",
                BITREPOSITORY_SBPILLAR_MOUNTPOINT + "=" + bitrepoPath.toAbsolutePath(),
                URL_TO_BATCH_DIR_PROPERTY + "=" + batchPath.toAbsolutePath()
        });
    }
}
