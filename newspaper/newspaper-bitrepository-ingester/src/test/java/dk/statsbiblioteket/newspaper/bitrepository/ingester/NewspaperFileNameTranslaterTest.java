package dk.statsbiblioteket.newspaper.bitrepository.ingester;


import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class NewspaperFileNameTranslaterTest {
    public static final String FILENAME = "B400022028241-RT1/400022028241-14/1795-06-13-01/foo";
    public static final String FILEID = "B400022028241-RT1_400022028241-14_1795-06-13-01_foo";

    @Test
    public void goodCaseRegistrationTest() {
        String translatedFileID = NewspaperFileNameTranslater.getFileID(FILENAME);
        assertEquals(translatedFileID, FILEID);
        String reversedFilename = NewspaperFileNameTranslater.getFileName(translatedFileID);
        assertEquals(reversedFilename, FILENAME);
    }
}
