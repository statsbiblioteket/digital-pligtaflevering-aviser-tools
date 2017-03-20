package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

/**
 *
 */
public class BitRepositoryModuleTest {
    @Test
    public void provideEncodePublicURLForFileIDTest() throws Exception {
        BitRepositoryModule bitRepositoryModule = new BitRepositoryModule();
        // DPA-99 - https://sbprojects.statsbiblioteket.dk/jira/browse/DPA-99
        String bigmagUrl = "http://localhost:58709/var/file1pillar/files/dpaviser/folderDir/";
        Function<String, String> f = bitRepositoryModule.provideEncodePublicURLForFileID(bigmagUrl);

        String fileID = "dl_20170226_rt1/viborgstiftsfolkeblad/pages/20170226_viborgstiftsfolkeblad_section04_page001_vsf20170226x14#0001.pdf";

        final String expected = "http://localhost:58709/var/file1pillar/files/dpaviser/folderDir/dl_20170226_rt1/viborgstiftsfolkeblad/pages/20170226_viborgstiftsfolkeblad_section04_page001_vsf20170226x14%25230001.pdf";

        Assert.assertEquals(expected, f.apply(fileID));
    }
}
