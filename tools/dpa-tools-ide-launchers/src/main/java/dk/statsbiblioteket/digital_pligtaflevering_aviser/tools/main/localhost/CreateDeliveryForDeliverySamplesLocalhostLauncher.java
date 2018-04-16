package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.localhost;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.CreateDeliveryMain;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.nio.file.Path;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * Launcher suitable for invoking CreateDeliveryMain from within an IDE using the delivery-samples folder.
 */
public class CreateDeliveryForDeliverySamplesLocalhostLauncher {

    public static void main(String[] args) {

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLocalhostLauncher.class, "delivery-samples");
        Path doneDirPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLocalhostLauncher.class, "delivery-samples-done-dir");

        CreateDeliveryMain.main(new String[]{
                "localhost/create-delivery.properties",
                "autonomous.agent=register-batch-trigger",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString(),
                CreateDeliveryMain.AUTONOMOUS_DONEDIR + "=" + doneDirPath.toString(),
                CreateDeliveryMain.DELIVERY_READY_FILENAME + "=transfer_acknowledged"
        });
    }
}

