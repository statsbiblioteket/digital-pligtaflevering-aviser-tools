package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */

public class VeraPDFAnalyzeMainTest {
    @org.junit.Test
    public void invocationTest() throws URISyntaxException {

        String bitrepoDirPathInWorkspace = "bitrepositorystub-storage";
        String batchDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = VeraPDFAnalyzeMainTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.

        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        Path bitrepoPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, bitrepoDirPathInWorkspace);

        VeraPDFAnalyzeMain.main(new String[]{
                "verapdf-analyze-vagrant.properties"
        });
    }
}
