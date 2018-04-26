package dk.statsbiblioteket.newspaper.bitrepository.ingester;

/**
 * Class to handle translation between filename in the abstract tree and the bitrepository id.  
 */
public class NewspaperFileNameTranslater {

    private static final String FS_PATH_SEPERATOR = "/";
    private static final String BITMAG_PATH_SEPERATOR = "_";
    
    /**
     * Gets the bitrepository id for the given filename
     * @param fileName The filename
     * @return String, the bitrepository id 
     */
    public static String getFileID(String fileName) {
        return fileName.replace(FS_PATH_SEPERATOR, BITMAG_PATH_SEPERATOR);
    }
    
    /**
     * Gets the filename for the given bitrepository id
     * @param fileID the ID of the file
     * @return String, the filename for the file in the abstract tree 
     */
    public static String getFileName(String fileID) {
        return fileID.replace(BITMAG_PATH_SEPERATOR, FS_PATH_SEPERATOR);
    }
    
}
