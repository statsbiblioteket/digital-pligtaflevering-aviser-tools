package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.localhost;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.CreateDeliveryMain;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.nio.file.Path;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 *
 */
public class CreateDeliveryForDeliverySamplesLocalhostLauncher {

    public static void main(String[] args) {

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLocalhostLauncher.class, "delivery-samples");

        CreateDeliveryMain.main(new String[]{
                "localhost/create-delivery.properties",
                "autonomous.agent=register-batch-trigger",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString()});
    }
}
