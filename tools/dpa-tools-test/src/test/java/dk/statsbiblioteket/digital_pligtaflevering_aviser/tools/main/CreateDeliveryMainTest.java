package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 *
 */
public class CreateDeliveryMainTest {
    @Test
    public void createPendingBatches() {
        // Look for delivery samples and invoke for each folder there
        CreateDeliveryMain.main(new String[]{
                "create-delivery.properties",
                "autonomous.agent=register-batch-trigger",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + getBatchFolder()});
    }

    /**
     * Get the folder where the testbatches is located during test in dev-environment
     *
     * @return
     */
    public String getBatchFolder() {
        String deliveryDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = null;
        try {
            l = CreateDeliveryMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.
        Path deliveryPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, deliveryDirPathInWorkspace);
        return deliveryPath.toString();
    }
}
