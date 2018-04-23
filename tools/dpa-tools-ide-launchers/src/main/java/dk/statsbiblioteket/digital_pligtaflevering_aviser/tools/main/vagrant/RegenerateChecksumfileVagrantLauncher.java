package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.vagrant;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main.RegenerateChecksumfileMain;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Launcher suitable for invoking IngesterMain from within an IDE using the delivery-samples folder.
 *
 */
public class RegenerateChecksumfileVagrantLauncher {

    public static void main(String[] args) throws Exception {

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(RegenerateChecksumfileVagrantLauncher.class, "delivery-samples");

        // http://stackoverflow.com/a/320595/53897
        URI l = RegenerateChecksumfileVagrantLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);



        RegenerateChecksumfileMain.main(new String[]{
                "vagrant/regenerate-checksumfile.properties",
                // FIXME: For now just a hardcoded roundtrip uuid.
                "uuid:2501557e-1583-4efc-9947-6328c2385f26"
        });
    }
}
