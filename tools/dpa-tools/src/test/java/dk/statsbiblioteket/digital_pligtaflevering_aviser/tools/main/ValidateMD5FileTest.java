package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FilePathToChecksumPathConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.DeliveryMD5Validation;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import org.junit.Before;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Test of checksum-consistency component
 */
public class ValidateMD5FileTest {

    private IngesterMain.IngesterModule ingesterModule = new IngesterMain.IngesterModule();
    private String batchFolder;


    // e.g. Creating an similar object and share for all @Test
    @Before
    public void runBeforeTestMethod() {
        batchFolder = getBatchFolder();
    }


    @org.junit.Test
    public void analyzeDeliveriesAgainstChecksums() throws Exception {

        String folder = getBatchFolder();

        //Check against expected function
        DeliveryMD5Validation md5Validator = new DeliveryMD5Validation(folder, "checksums.txt", ingesterModule.provideFilePathConverter(), "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
        boolean validated = md5Validator.validation("dl_20160811_rt1");
        assertEquals("Validation of correspondance between checksum-file and content in testdeliveries", validated, true);

        //Check against wron function
        md5Validator = new DeliveryMD5Validation(folder, "checksums.txt", providePreviousFilePathConverter(), "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
        validated = md5Validator.validation("dl_20160811_rt1");
        assertEquals("Validation of correspondance between checksum-file and content in testdeliveries", validated, false);
    }

    // MD5SUMS.txt are not present anymore and the test is rather misleading.

//    /**
//     * MD5SUMS.txt is the old checksum format from Infomedia, the deliveries is not supplied in that way anymore, but
//     * the functionality is still tested
//     *
//     * @throws Exception
//     */
//    @org.junit.Test
//    public void analyzeDeliveriesAgainstMD5SUMS() throws Exception {
//
//        String folder = getBatchFolder();
//
//        //Check against wrong function
//        DeliveryMD5Validation md5Validator = new DeliveryMD5Validation(folder, "MD5SUMS.txt", ingesterModule.provideFilePathConverter(), "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
//        boolean validated = md5Validator.validation("dl_20160811_rt1");
//        assertEquals("Validation of correspondance between checksum-file and content in testdeliveries", false, validated);
//
//        //Check against expected function
//        md5Validator = new DeliveryMD5Validation(folder, "MD5SUMS.txt", providePreviousFilePathConverter(), "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
//        validated = md5Validator.validation("dl_20160811_rt1");
//        assertEquals("Validation of correspondance between checksum-file and content in testdeliveries", true, validated);
//    }


    /**
     * Provide Function for converting filePath as written in MD5SUMS.txt
     * The FilePathToChecksumPathConverter supplied by this method demonstartes the encoding of filepath as used in old deliveries from Infomedia
     * @return and ID for the fileContent
     */
    private FilePathToChecksumPathConverter providePreviousFilePathConverter() {

        //This formatter uses the old checksum-format
        return (path1, batchName) -> path1.getFileName().toString();
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

        Path batchPath = MavenProjectsHelper.getRequiredPathTowardsRoot(startDir, batchDirPathInWorkspace);
        return batchPath.toString();
    }
}
