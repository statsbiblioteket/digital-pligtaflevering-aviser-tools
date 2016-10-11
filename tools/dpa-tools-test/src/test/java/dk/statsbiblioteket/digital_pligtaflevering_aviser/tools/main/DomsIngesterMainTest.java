package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class DomsIngesterMainTest {
    @Test
    public void domsIngestB20160811_RT1() throws URISyntaxException {
        String batchDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = BitrepositoryStubIngesterMainTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.

        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);

        DomsIngesterMain.main(new String[]{
                "doms-ingester.properties",
                "iterator.filesystem.batches.folder=" + batchPath.toAbsolutePath()
        });
        System.exit(1);
    }
}
