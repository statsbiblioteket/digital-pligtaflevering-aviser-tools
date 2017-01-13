package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.FilePathToChecksumPathConverter;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.BatchMD5Validation;
import org.junit.Before;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test of checksum-consistency component
 */
public class ValidateMD5FileTest {

    private String batchFolder;


    // e.g. Creating an similar object and share for all @Test
    @Before
    public void runBeforeTestMethod() {
        batchFolder = getBatchFolder();
    }


    @org.junit.Test
    public void analyzeDeliveriesAgainstChecksums1() throws Exception {

        String folder = getBatchFolder();
        BatchMD5Validation md5Validator = new BatchMD5Validation(folder, "checksums.txt", provideFilePathConverter(true), "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
        boolean bb = md5Validator.validation("dl_20160811_rt1");
        List<String> results = md5Validator.getValidationResult();

        assertEquals("CHECKSUM", bb, true);
    }


    @org.junit.Test
    public void analyzeDeliveriesAgainstChecksums2() throws Exception {

        String folder = getBatchFolder();

        BatchMD5Validation md5Validator = new BatchMD5Validation(folder, "MD5SUMS.txt", provideFilePathConverter(false), "transfer_acknowledged,transfer_complete,checksums.txt,MD5SUMS.txt");
        boolean bb = md5Validator.validation("dl_20160811_rt1");
        List<String> results = md5Validator.getValidationResult();

        assertEquals("CHECKSUM", bb, true);
    }


    /**
     * Provide Function for converting filePath a for
     * @return and ID for the fileContent
     */
    public FilePathToChecksumPathConverter provideFilePathConverter(boolean InfomediaFormat) {

        if(InfomediaFormat) {
            //This should be used for the checksums in the deliveries from Infomedia
            return (path1, batchName) -> path1.getFileName().toString();
        } else {
            //This should be used for the checksums in the specifications
            return (path1, batchName) -> Paths.get(batchFolder, batchName).relativize(path1).toString();
        }
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