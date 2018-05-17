package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;

import java.nio.file.Path;

/**
 * Launcher suitable for invoking IngesterMain from within an IDE using the delivery-samples folder.
 */
public class CheckRegeneratedChecksumfileLauncher {

    public static void main(String[] args) throws Exception {

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLauncher.class, "delivery-samples");

        CheckRegeneratedChecksumfileMain.main(new String[]{
                "check-regenerated-checksumfile.properties",
                ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString()
        });
    }
}
