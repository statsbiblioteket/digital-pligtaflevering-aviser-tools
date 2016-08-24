package dk.statsbiblioteket.newspaper.bitrepository.ingester;


import java.io.IOException;
import java.net.URL;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class BatchImageLocatorTest {
    public static final String BATCH_DIR_URL = "file://batchDirUrl";
    protected static String DEFAULT_MD5_CHECKSUM = "1234cccccccc4321";
    private BatchImageLocator locator;
    private TreeIterator treeIterator;

    @BeforeMethod
    public void setupBatchImageLocator() {
        treeIterator = mock(TreeIterator.class);
        locator = new BatchImageLocator(treeIterator, BATCH_DIR_URL);
        verifyNoMoreInteractions(treeIterator);
    }

    @Test
    public void simpleFileTest() throws IOException {
        when(treeIterator.hasNext()).thenReturn(true);
        String firstFileName = "first-image.jp2";
        FileAttributeParsingEvent fileEvent = mock(FileAttributeParsingEvent.class);
        when(fileEvent.getName()).thenReturn(getEventNameForFile(firstFileName));
        when(fileEvent.getChecksum()).thenReturn(DEFAULT_MD5_CHECKSUM);
        when(treeIterator.next()).thenReturn(fileEvent);
        IngestableFile firstFile = locator.nextFile();
        assertEquals(firstFile.getFileID(), firstFileName);
        assertEquals(firstFile.getLocalUrl(), new URL(BATCH_DIR_URL + "/" + firstFileName));
        assertEquals(firstFile.getChecksum().getChecksumValue(), getChecksum(DEFAULT_MD5_CHECKSUM).getChecksumValue());
        assertEquals(firstFile.getChecksum().getChecksumSpec(), getChecksum(DEFAULT_MD5_CHECKSUM).getChecksumSpec());
    }

    @Test
    public void fullPathTest() throws IOException {
        when(treeIterator.hasNext()).thenReturn(true);
        String firstFileName = "B400022028241-RT1/400022028241-14/1795-06-13-01/adresseavisen1759-1795-06-13-01-0006.jp2";
        FileAttributeParsingEvent fileEvent = mock(FileAttributeParsingEvent.class);
        when(fileEvent.getName()).thenReturn(getEventNameForFile(firstFileName));
        when(fileEvent.getChecksum()).thenReturn(DEFAULT_MD5_CHECKSUM);
        when(treeIterator.next()).thenReturn(fileEvent);
        IngestableFile firstFile = locator.nextFile();
        String expectedFileID = firstFileName.replace('/', '_');
        assertEquals(firstFile.getFileID(), expectedFileID);
        assertEquals(firstFile.getLocalUrl(), new URL(BATCH_DIR_URL + "/" + firstFileName));
    }

    @Test
    public void otherNodeTest() throws IOException {
        when(treeIterator.hasNext()).thenReturn(true).thenReturn(true);
        String firstFileName = "first-image.jp2";
        FileAttributeParsingEvent fileEvent = mock(FileAttributeParsingEvent.class);
        when(fileEvent.getName()).thenReturn(getEventNameForFile(firstFileName));
        when(fileEvent.getChecksum()).thenReturn(DEFAULT_MD5_CHECKSUM);
        when(treeIterator.next()).thenReturn(fileEvent);
        when(treeIterator.next()).
                thenReturn(new NodeBeginsParsingEvent("UNMATCHED", null)).
                thenReturn(fileEvent);
        IngestableFile firstFile = locator.nextFile();
        assertEquals(firstFile.getFileID(), firstFileName);
        assertEquals(firstFile.getLocalUrl(), new URL(BATCH_DIR_URL + "/" + firstFileName));
    }

    private ChecksumDataForFileTYPE getChecksum(String checksum) {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        checksumData.setCalculationTimestamp(CalendarUtils.getNow());
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        checksumData.setChecksumSpec(checksumSpec);
        return checksumData;
    }

    private String getEventNameForFile(String eventName) {
        return eventName + "/contents";
    }
}
