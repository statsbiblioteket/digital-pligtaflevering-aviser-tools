package dk.statsbiblioteket.newspaper.bitrepository.ingester.utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Validator for md5 validation of digital newspaper batches
 */
public class BatchMD5Validation {
    private String batchFolder;
    private Map<String, String> md5Map = new HashMap<String, String>();

    public BatchMD5Validation(String batchFolder) {
        this.batchFolder = batchFolder;
    }

    /**
     * Validate a specified batch by reading the MD5SUMS, and confirm that all files in the folder has files with checksums matching the sṕecifications in the file
     * @param batchName The name of the batch where the checksum should be found
     */
    public void validation(String batchName) {
        try(BufferedReader br = new BufferedReader(new FileReader(this.batchFolder + File.separator+batchName + File.separator + "MD5SUMS.txt"))) {
            String line = br.readLine();

            while (line != null) {
                //Each line in the file is stored in a String, a line consists of a checksum and a filename, they a seperated by 2 spaces.
                //Example [8bd4797544edfba4f50c91c917a5fc81  verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf]
                line = br.readLine();
                if(line != null) {
                    String[] split = line.split("  ");
                    String filename = batchName + File.separator + split[1];
                    String checksum = split[0];
                    md5Map.put(filename, checksum);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String getExpectedMD5(String filename) {
        return md5Map.get(filename);
    }
}