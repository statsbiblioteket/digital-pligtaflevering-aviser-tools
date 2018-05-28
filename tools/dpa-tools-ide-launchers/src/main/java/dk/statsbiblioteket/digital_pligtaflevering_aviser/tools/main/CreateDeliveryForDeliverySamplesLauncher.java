package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.nio.file.Path;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER;

/**
 * Launcher suitable for invoking CreateDeliveryMain from within an IDE using the delivery-samples folder.
 */
public class CreateDeliveryForDeliverySamplesLauncher {

    public static void main(String[] args) {

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLauncher.class, "delivery-samples");
        Path doneDirPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLauncher.class, "delivery-samples-done-dir");

        CreateDeliveryMain.main(new String[]{
                "create-deliveries.properties",
                ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString(),
                CreateDeliveryMain.AUTONOMOUS_DONEDIR + "=" + doneDirPath.toString()});
    }
}
