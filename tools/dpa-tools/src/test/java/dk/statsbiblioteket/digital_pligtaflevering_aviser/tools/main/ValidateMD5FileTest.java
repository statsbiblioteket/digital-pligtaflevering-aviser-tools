package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.BatchMD5Validation;
import org.junit.Before;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test af xml validation against xsd.
 * This component uses two different xsd-files Article.xsd and PdfInfo.xsd.
 * It looks for the rootTag in the xml, and finds the schema expected to match the name of the rootTag
 */
public class ValidateMD5FileTest {



    // e.g. Creating an similar object and share for all @Test
    @Before
    public void runBeforeTestMethod() {
    }


    @org.junit.Test
    public void analyzeDeliveriesFolderTest() throws Exception {


        String folder = getBatchFolder();


        BatchMD5Validation md5Validator = new BatchMD5Validation(folder, "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
        boolean bb = md5Validator.validation("dl_20160811_rt1");
        List<String> results = md5Validator.getValidationResult();

        assertEquals("CHECKSUM", bb, true);


    }





    /**
     * Get the folder where the testbatches is located during test in dev-environment
     *
     * @return
     */
    private String getBatchFolder() {
        String batchDirPathInWorkspace = "delivery-samples";

        // http://stackoverflow.com/a/320595/53897
        URI l = null;
        try {
            l = CreateDeliveryMain.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path startDir = Paths.get(l);

        // Look for the first instance of batchDir in the directories towards the root of the file system.
        // This will work anywhere in the source tree.  StreamEx provide three argument iterate() in Java 8.
        Path batchPath = AutonomousPreservationToolHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        return batchPath.toString();
    }


}