package dk.statsbiblioteket.newspaper.promptdomsingester.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Validator for md5 validation of digital newspaper batches
 */
public class BatchMD5Validation {
    private String batchFolder;

    public BatchMD5Validation(String batchFolder) {
        this.batchFolder = batchFolder;
    }

    /**
     * Validate a specified batch by reading the MD5SUMS, and confirm that all files in the folder has files with checksums matching the sṕecifications in the file
     * @param batchName
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean validation(String batchName) throws IOException, NoSuchAlgorithmException {
        HashMap<String, String> hmp = new HashMap<String, String>();
        try(BufferedReader br = new BufferedReader(new FileReader(this.batchFolder+File.separator+batchName+File.separator+"MD5SUMS.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
                if(line != null) {
                    String[] split = line.split("  ");
                    hmp.put(split[1], split[0]);
                }
            }
        }

        for(String file: hmp.keySet()) {
            String expectedMd5 = hmp.get(file);
            String actualMd5 = this.getFileChecksum(MessageDigest.getInstance("md5"), new File(this.batchFolder+File.separator+batchName+File.separator+file));
            if(!expectedMd5.equals(actualMd5)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Get the md5 checksum of a file
     * Kopieret fra følgende link
     * http://howtodoinjava.com/core-java/io/how-to-generate-sha-or-md5-file-checksum-hash-in-java/
     * @param digest
     * @param file
     * @return
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
