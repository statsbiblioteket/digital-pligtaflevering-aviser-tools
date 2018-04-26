package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.nio.file.Path;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * Launcher suitable for invoking CreateDeliveryMain from within an IDE using the delivery-samples folder.
 */
public class CreateDeliveryForDeliverySamplesLauncher {

    public static void main(String[] args) {

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLauncher.class, "delivery-samples");
        Path doneDirPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLauncher.class, "delivery-samples-done-dir");

        CreateDeliveryMain.main(new String[]{
                "create-delivery.properties",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString(),
                CreateDeliveryMain.AUTONOMOUS_DONEDIR + "=" + doneDirPath.toString()});
    }
}
