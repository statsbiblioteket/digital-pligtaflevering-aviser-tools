package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import org.junit.Test;

import java.nio.file.Path;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 *
 */
public class CreateDeliveryMainTest {
    @Test
    public void createPendingBatches() {

        Path deliveryPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(this, "delivery-samples");

        CreateDeliveryMain.main(new String[]{
                "create-delivery.properties",
                "autonomous.agent=register-batch-trigger",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString()});
    }

}
