package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.localhost;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.VeraPDFInvokeMain;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.nio.file.Path;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.BitRepositoryModule.BITREPOSITORY_SBPILLAR_MOUNTPOINT;
import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.URL_TO_BATCH_DIR_PROPERTY;


public class VeraPDFInvokeAllLocalhostLauncher {
    public static void main(String[] args) throws Exception {

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(VeraPDFInvokeAllLocalhostLauncher.class, "delivery-samples");
        Path bitrepoPath = MavenProjectsHelper.getRequiredPathTowardsRoot(VeraPDFInvokeAllLocalhostLauncher.class, "bitrepositorystub-storage");

        VeraPDFInvokeMain.main(new String[]{
                "localhost/verapdf-invoke-all-vagrant.properties",
                BITREPOSITORY_SBPILLAR_MOUNTPOINT + "=" + bitrepoPath.toAbsolutePath(),
                URL_TO_BATCH_DIR_PROPERTY + "=" + batchPath.toAbsolutePath()
        });
    }
}
