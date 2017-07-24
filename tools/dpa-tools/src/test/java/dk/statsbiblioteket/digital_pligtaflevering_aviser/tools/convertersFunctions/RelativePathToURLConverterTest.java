package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

/**
 *
 */
public class RelativePathToURLConverterTest {
    @Test
    public void fileURLs() throws Exception {
        final String root = "/sbftp-home/infomed2/stage/";
        final String p = "dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11#0001";
        Assert.assertEquals(
                "file:/sbftp-home/infomed2/stage/dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11%230001",
                new RelativePathToURLConverter(root).apply(Paths.get(p)).toString());
    }

}
