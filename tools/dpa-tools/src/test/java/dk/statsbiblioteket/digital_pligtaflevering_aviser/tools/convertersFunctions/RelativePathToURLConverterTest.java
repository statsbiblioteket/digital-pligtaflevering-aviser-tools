package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 *
 */
public class RelativePathToURLConverterTest {
    @Test
    public void sbftphome1() throws Exception {
        final String root = "file:/sbftp-home/infomed2/stage/";
        final String p = "dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11#0001";
        Assert.assertEquals(
                "file:/sbftp-home/infomed2/stage/dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11%230001",
                new RelativePathToURLConverter(new java.net.URL(root)).apply(Paths.get(p)).toString());
    }

    @Test
    public void root1() throws Throwable {
        final String root = "file:/delivery-samples/";
        final String p = "dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11#0001";

        try {
            String url = new RelativePathToURLConverter(new URL(root)).apply(Paths.get(p)).toString();
            Assert.assertEquals("file:/delivery-samples/dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11%230001", url);
        } catch (RuntimeException e) {
            // instead of black magic suggested at https://stackoverflow.com/a/20759785/53897
            throw e.getCause();
        }

    }


    @Test
    public void root2() throws Throwable {
        final String root = "file:///tmp/";
        final String p = "dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11#0001";

        try {
            String l = new RelativePathToURLConverter(new java.net.URL(root)).apply(Paths.get(p)).toString();
            Assert.assertEquals("file:/tmp/dl_20131231_rt2/vejleamtsfolkeblad/pages/20131231_vejleamtsfolkeblad_section01_page001_vaf20131231x11%230001", l);
        } catch (RuntimeException e) {
            // instead of black magic suggested at https://stackoverflow.com/a/20759785/53897
            throw e.getCause();
        }

    }

}
