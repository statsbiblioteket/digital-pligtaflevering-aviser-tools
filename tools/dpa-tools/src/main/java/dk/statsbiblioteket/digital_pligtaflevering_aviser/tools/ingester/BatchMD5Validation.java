package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * Validator for md5 validation of digital newspaper batches
 */
public class BatchMD5Validation {
    final private String batchFolder;
    private String checksumFileName;
    private boolean infomediaFormat = false;
    final private HashSet<String> ignoredFiles = new HashSet<String>();
    private Map<String, String> cashedFileListMap = new HashMap<String, String>();
    private List<String> validationResult = new ArrayList<String>();

    public BatchMD5Validation(String batchFolder, String checksumFileName, boolean infomediaFormat, String ignoredFilesString) {
        this.batchFolder = batchFolder;
        this.checksumFileName = checksumFileName;
        this.infomediaFormat = infomediaFormat;
        for(String ignoredFile : ignoredFilesString.split(",")) {
            ignoredFiles.add(ignoredFile);
        }
    }

    /**
     * Validate a specified batch by reading the checksums, and confirm that all files listed in "checksums.txt" does actually exist and that the real files has the same checksum.
     *
     * @param batchName
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean validation(String batchName) throws IOException, NoSuchAlgorithmException {
        //Read the checksums from the file "checksums.txt" and insert it into a hashmap with filenames as keys and checksums as values
        File checksumFile = Paths.get(batchFolder, batchName, checksumFileName).toFile();
        if(!checksumFile.exists()) {
            validationResult.add("The checksumfile " + checksumFile.getAbsolutePath() + " is missing");
            return false;
        }

        //Start reading the checksum-file, and store all checksums in a hashmap
        Map<String, String> md5Map = new HashMap<String, String>();
        try(BufferedReader br = new BufferedReader(new FileReader(checksumFile))) {
            String line = br.readLine();
            while (line != null) {
                //Each line in the file is stored in a String, a line consists of a checksum and a filename, they a seperated by 2 spaces.
                //Example [8bd4797544edfba4f50c91c917a5fc81  verapdf/udgave1/pages/20160811-verapdf-udgave1-page001.pdf]
                if(line != null) {
                    String[] split = line.split("\\s+");
                    String filename = split[1];
                    String checksum = split[0];
                    md5Map.put(filename, checksum);
                }
                line = br.readLine();
            }
        }

        //Walk through the filesystem in tha batch and confirm that all files inside the batch is also mentioned in the checksum-file
        //It is possible ti indicate some specific files whish should be ignored, thease is not part of the validation
        Files.walk(Paths.get(Paths.get(batchFolder, batchName).toFile().getAbsolutePath()))
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    String fileIdMatchingChecksumfile = provideFilePathConverter(batchName).apply(filePath);
                    if (ignoredFiles.contains(fileIdMatchingChecksumfile)) {
                        //This file is one of the ignored files, just contionue without doing anything
                    } else if(!md5Map.containsKey(fileIdMatchingChecksumfile)) {
                        validationResult.add("There is missing a file reference in " + checksumFileName + " : " + filePath.toFile().getAbsolutePath());
                    } else {
                        cashedFileListMap.put(fileIdMatchingChecksumfile, md5Map.remove(fileIdMatchingChecksumfile));
                    }
                });

        //Make sure that all files listed in "checksums.txt" exists and has the correct checksum
        for(String fileName: md5Map.keySet()) {
            File file = Paths.get(batchFolder, batchName, fileName).toFile();
            if(file.exists()) {
                String expectedMd5 = md5Map.get(fileName);
                String actualMd5 = getFileChecksum(MessageDigest.getInstance("md5"), file);
                if(!expectedMd5.equals(actualMd5)) {
                    //If the checksum of the delivered file and the checksum of the file in "MD5SUMS.txt" does not match, raise an error
                    validationResult.add(file.getAbsolutePath() + " expectedMd5: " + expectedMd5 + " actualMd5:" + actualMd5);
                }
            } else {
                //If the file that is claimed to exist in the "checksums.txt" can not be found, raise an error
                validationResult.add("There is missing a file " + checksumFile.getAbsolutePath() +" claims is existing  : " + file);
            }
        }
        return validationResult.size()==0;
    }

    /**
     * Function for conversion from Path to the fileformat described in the checksum-file
     * @param batchName
     * @return
     */
    private Function<Path, String> provideFilePathConverter(String batchName) {
        if(infomediaFormat) {
            return path1 -> path1.getFileName().toString();
        } else {
            return path1 -> Paths.get(batchFolder, batchName).relativize(path1).toString();
        }
    }

    public String getChecksum(Path filePath) {
        if(infomediaFormat) {
            return cashedFileListMap.get(filePath.getFileName().toString());
        } else {
            return cashedFileListMap.get(filePath.toString());
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
