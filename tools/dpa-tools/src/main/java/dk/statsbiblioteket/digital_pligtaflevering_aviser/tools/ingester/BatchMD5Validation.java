package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Validator for md5 validation of digital newspaper batches
 */
public class BatchMD5Validation {
    private String batchFolder;
    private HashSet<String> ignoredFiles = new HashSet<String>();
    private Map<String, String> md5Map = new HashMap<String, String>();
    private List<String> validationResult = new ArrayList<String>();

    public BatchMD5Validation(String batchFolder, String ignoredFilesString) {
        this.batchFolder = batchFolder;
        for(String ignoredFile : ignoredFilesString.split(",")) {
            ignoredFiles.add(ignoredFile);
        }
    }

    /**
     * Validate a specified batch by reading the MD5SUMS, and confirm that all files listed in "checksums.txt" does actually exist and that the real files has the same checksum.
     *
     * @param deliveryName The deliveryName equals the name of the folder inside the deliveryFolder
     * @return true if tha validation passed successfully
     * @throws IOException If files can not be read
     * @throws NoSuchAlgorithmException If md5 is unknown
     */
    public boolean validation(String deliveryName) throws IOException, NoSuchAlgorithmException, Exception {
        boolean validationResponse = true;
        //Read the checksums from the file "checksums.txt" and insert it into a hashmap with filenames as keys and checksums as values

        try(BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(batchFolder, deliveryName, "checksums.txt"))) {
            String line = br.readLine();

            while (line != null) {
                //Each line in the file is stored in a String, a line consists of a checksum and a filename, they a seperated by 2 spaces.
                //Example [8bd4797544edfba4f50c91c917a5fc81  verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf]
                if(line != null) {
                    String[] split = line.split("[,\\s]\\s*");
                    //Any line containing something must contain bot file and checksum
                    if(split.length != 0 && split.length != 2) {
                        throw new Exception("");
                    }
                    String filename = split[1];
                    String checksum = split[0];

                    md5Map.put(filename, checksum);
                }
                line = br.readLine();
            }
        }

        //Validate that all files in the folder does exist in "checksums.txt" except the ignored files
        Set<String> files = new HashSet<String>();
        listFiles(Paths.get(batchFolder, deliveryName).toFile().getAbsolutePath(), files);
        for(String ignoredFile : ignoredFiles) {
            files.remove(Paths.get(batchFolder, deliveryName).toFile().getAbsolutePath() + File.separator + ignoredFile);
        }
        for(String fileInBatchFolder : files) {
            //It is only a fail if
            if(!md5Map.containsKey(fileInBatchFolder.replaceFirst(Paths.get(batchFolder, deliveryName).toFile().getAbsolutePath() + File.separator, ""))) {
                validationResult.add("There is missing a file reference in \"checksums.txt\" : " + fileInBatchFolder);
                validationResponse = false;
            }
        }
        //TODO: FIX TO NEW DELIVERIES

        //Make sure that all files listed in "MD5SUMS.txt" exists and has the correct checksum
        for(String fileName: md5Map.keySet()) {
            File file = Paths.get(batchFolder, deliveryName, fileName).toFile();
            if(file.exists()) {
                String expectedMd5 = md5Map.get(fileName);
                String actualMd5 = getFileChecksum(MessageDigest.getInstance("md5"), file);
                if(!expectedMd5.equals(actualMd5)) {
                    //If the checksum of the delivered file and the checksum of the file in "checksums.txt" does not match, raise an error
                    validationResult.add(file.getAbsolutePath() + " expectedMd5: " + expectedMd5 + " actualMd5:" + actualMd5);
                    validationResponse = false;
                }
            } else {
                //If the file that is claimed to exist in the "MD5SUMS.txt" can not be found, raise an error
                validationResult.add("There is missing a file : " + file);
                validationResponse = false;
            }
        }
        return validationResponse;
    }

    /**
     * Get the currently loaded list of checksums
     * @return list of checksums as map
     */
    public Map<String, String> getCurrentLoadedMap() {
        return md5Map;
    }

    /**
     * Get a list of files inside a directory, the function is implemented as a recursive function.
     * The result is written into the parameter delivered to the function, like the way it is normally done in functional programming languages
     * @param directoryName The directory from where the files should be listed
     * @param files The list of files to get appended
     */
    public void listFiles(String directoryName, Set<String> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                listFiles(file.getAbsolutePath(), files);
            }
        }
    }

    /**
     * Get the result af validating checksums of all files in the Batch
     * @return Validation result as a list of messages
     */
    public List<String> getValidationResult() {
        return validationResult;
    }



    /**
     * Get the md5 checksum of a file
     * Copied from the following link
     * http://howtodoinjava.com/core-java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
     * @param digest Messagedigester
     * @param file Foleobject to be read
     * @return The checksum as a string
     * @throws IOException
     */
    private String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }
}
