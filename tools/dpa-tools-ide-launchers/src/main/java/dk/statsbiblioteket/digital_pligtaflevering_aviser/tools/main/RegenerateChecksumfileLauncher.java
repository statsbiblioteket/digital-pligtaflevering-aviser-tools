package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Launcher suitable for invoking IngesterMain from within an IDE using the delivery-samples folder.
 *
 */
public class RegenerateChecksumfileLauncher {

    public static void main(String[] args) throws Exception {

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(RegenerateChecksumfileLauncher.class, "delivery-samples");

        // http://stackoverflow.com/a/320595/53897
        URI l = RegenerateChecksumfileLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        RegenerateChecksumfileMain.main(new String[]{
                "regenerate-checksumfile.properties"
        });
    }
}
