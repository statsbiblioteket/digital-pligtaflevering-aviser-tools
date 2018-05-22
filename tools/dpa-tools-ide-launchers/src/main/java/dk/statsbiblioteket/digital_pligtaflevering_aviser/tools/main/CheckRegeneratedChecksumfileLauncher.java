package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;

import java.nio.file.Path;

/**
 * Launcher suitable for invoking IngesterMain from within an IDE using the delivery-samples folder.
 */
public class CheckRegeneratedChecksumfileLauncher {

    public static void main(String[] args) throws Exception {

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        Path deliveryPath = MavenProjectsHelper.getRequiredPathTowardsRoot(CreateDeliveryForDeliverySamplesLauncher.class, "delivery-samples");

        CheckRegeneratedChecksumfileMain.main(new String[]{
                "check-regenerated-checksumfile.properties",
                ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER + "=" + deliveryPath.toString()
        });
    }
}
