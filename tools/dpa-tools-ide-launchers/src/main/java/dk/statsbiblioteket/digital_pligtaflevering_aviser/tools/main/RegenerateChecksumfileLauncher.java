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

        // for jaxb-impl 2.3.0 under Java 10 - https://github.com/javaee/jaxb-v2/issues/1197
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(RegenerateChecksumfileLauncher.class, "delivery-samples");

        // http://stackoverflow.com/a/320595/53897
        URI l = RegenerateChecksumfileLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path startDir = Paths.get(l);

        RegenerateChecksumfileMain.main(new String[]{
                "regenerate-checksumfile.properties"
        });
    }
}
