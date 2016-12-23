package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;

/**
 *
 */

public class ValidateXMLMainTest {


    @org.junit.Test
    public void invocationTest() throws URISyntaxException {


        File f = new File("/Article.xsd");
        boolean bb = f.exists();

        /*String bitrepoDirPathInWorkspace = "bitrepositorystub-storage";
        String batchDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = ValidateXMLMainTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.

        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        Path bitrepoPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, bitrepoDirPathInWorkspace);

        InvokeVeraPdfMain.main(new String[]{
                "invoke-verapdf-vagrant.properties",
                BITREPOSITORY_SBPILLAR_MOUNTPOINT + "=" + bitrepoPath.toAbsolutePath(),
                URL_TO_BATCH_DIR_PROPERTY + "=" + batchPath.toAbsolutePath()
        });*/

        ValidateXMLMain.main(new String[]{
                "xmlvalidate-vagrant.properties",
        });
    }
}
