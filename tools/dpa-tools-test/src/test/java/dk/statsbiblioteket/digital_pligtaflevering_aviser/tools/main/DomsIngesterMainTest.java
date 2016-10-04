package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import one.util.streamex.StreamEx;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class DomsIngesterMainTest {
    @Test
    public void domsIngestB20160811_RT1() throws URISyntaxException {
        String batchDir = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = DomsIngesterMainTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path dir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.

        Path batchPath = StreamEx
                .iterate(dir, p -> p != null, p -> p.getParent()) // walk up to root
                .map(p -> p.resolve(batchDir))
                .filter(p -> p.toFile().exists())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(batchDir + " not found towards root of " + l));

        DomsIngesterMain.main(new String[]{
                "doms-ingester.properties",
                "iterator.filesystem.batches.folder=" + batchPath.toAbsolutePath()
        });
        System.exit(1);
    }
}
