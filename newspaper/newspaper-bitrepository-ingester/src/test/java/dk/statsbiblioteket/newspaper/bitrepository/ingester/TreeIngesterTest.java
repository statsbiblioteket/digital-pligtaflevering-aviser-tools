package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.modify.putfile.PutFileClient;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.ParallelOperationLimiter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.TreeIngester;

public class TreeIngesterTest {
    public final static String TEST_COLLECTION_ID = "testCollection";
    protected static final int DEFAULT_MAX_NUMBER_OF_PARALLEL_PUTS = 10;
    protected static final int DEFAULT_MAX_RETRIES = 3;
    protected static final String DEFAULT_BASEURL = "http://bitfinder.statsbiblioteket.dk/avis/";
    private Batch testBatch;
    private BatchImageLocator fileLocator;
    private PutFileClient putFileClient;
    private ResultCollector resultCollector;
    private TreeIngester treeIngester;
    private DomsJP2FileUrlRegister urlRegister;
    ParallelOperationLimiter operationLimiter;

    @BeforeMethod
    public void setupTreeIngester() {
        fileLocator = mock(BatchImageLocator.class);
        putFileClient = new PutFileClientStub();
        resultCollector = mock(ResultCollector.class);
        urlRegister = mock(DomsJP2FileUrlRegister.class);
        operationLimiter = new ParallelOperationLimiter(DEFAULT_MAX_NUMBER_OF_PARALLEL_PUTS);
        testBatch = new Batch("123", 1);
        
    }

    @Test
    public void emptyTreeTest() throws InterruptedException {
    	treeIngester = new TreeIngester(TEST_COLLECTION_ID, operationLimiter, urlRegister, fileLocator, putFileClient, resultCollector, 
    	        DEFAULT_MAX_RETRIES, testBatch);
        treeIngester.performIngest();
    }

    @Test
    public void parallelPutTest() throws MalformedURLException, InterruptedException {
        putFileClient = mock(PutFileClient.class);
        int maxNumberOfParallelPuts = 2;
        ChecksumDataForFileTYPE checksum = getChecksum("aa");
        operationLimiter = new ParallelOperationLimiter(maxNumberOfParallelPuts);
        treeIngester = new TreeIngester(TEST_COLLECTION_ID, operationLimiter, urlRegister, fileLocator, putFileClient, resultCollector, 
                DEFAULT_MAX_RETRIES, testBatch);
        IngestableFile firstFile =
                new IngestableFile("First-file", new URL("http://somewhere.someplace/first-file"), checksum, 0L,
                        "path:First-file");
        IngestableFile secondFile =
                new IngestableFile("Second-file", new URL("http://somewhere.someplace/second-file"), checksum, 0L,
                        "path:Second-file");
        IngestableFile thirdFile =
                new IngestableFile("Third-file", new URL("http://somewhere.someplace/third-file"), checksum, 0L,
                        "path:Third-file");
        IngestableFile fourthFile =
                new IngestableFile("Fourth-file", new URL("http://somewhere.someplace/fourthFile-file"), checksum, 0L,
                        "path:Fourth-file");
        when(fileLocator.nextFile()).thenReturn(firstFile).thenReturn(secondFile).thenReturn(thirdFile).thenReturn(fourthFile);

        //We need to run the ingest in a separate thread, as it will block.
        Thread t = new Thread(new TreeIngestRunner());
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(putFileClient, timeout(3000).times(1)).putFile(
                eq(TEST_COLLECTION_ID), eq(firstFile.getLocalUrl()), eq(firstFile.getFileID()), eq(0L),
                eq(checksum), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        verify(putFileClient, timeout(3000).times(1)).putFile(
                eq(TEST_COLLECTION_ID), eq(secondFile.getLocalUrl()), eq(secondFile.getFileID()), eq(0L),
                eq(checksum), (ChecksumSpecTYPE) isNull(), (EventHandler)anyObject(), (String) isNull());
        verifyNoMoreInteractions(putFileClient);

        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION_ID, null);
        firstFileComplete.setFileID(firstFile.getFileID());
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        verify(putFileClient, timeout(3000).times(1)).putFile(
                eq(TEST_COLLECTION_ID), eq(thirdFile.getLocalUrl()), eq(thirdFile.getFileID()), eq(0L),
                eq(checksum), (ChecksumSpecTYPE) isNull(), (EventHandler)anyObject(), (String) isNull());

        CompleteEvent secondFileComplete = new CompleteEvent(TEST_COLLECTION_ID, null);
        secondFileComplete.setFileID(secondFile.getFileID());
        eventHandlerCaptor.getValue().handleEvent(secondFileComplete);
        verify(putFileClient, timeout(3000).times(1)).putFile(
                eq(TEST_COLLECTION_ID), eq(fourthFile.getLocalUrl()), eq(fourthFile.getFileID()), eq(0L),
                eq(checksum), (ChecksumSpecTYPE) isNull(), (EventHandler)anyObject(), (String) isNull());
    }

    /**
     * Tests that the ingester correctly waits for the last put to complete before exiting.
     */
    @Test
    public void parallelPutCompletionTest() throws MalformedURLException, InterruptedException {
        putFileClient = mock(PutFileClient.class);
        int maxNumberOfParallelPuts = 1;
        ChecksumDataForFileTYPE checksum = getChecksum("aa");
        operationLimiter = new ParallelOperationLimiter(maxNumberOfParallelPuts);
        treeIngester = new TreeIngester(TEST_COLLECTION_ID, operationLimiter, urlRegister, fileLocator, putFileClient, resultCollector, 
                DEFAULT_MAX_RETRIES, testBatch);
        
        IngestableFile firstFile =
                new IngestableFile("First-file", new URL("http://somewhere.someplace/first-file"), checksum, 0L,
                        "path:First-file");
        when(fileLocator.nextFile()).thenReturn(firstFile).thenReturn(null);

        //We need to run the ingest in a separate thread, as it will block.
        TreeIngestRunner runner = new TreeIngestRunner();
        Thread t = new Thread(runner);
        t.start();

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(putFileClient, timeout(3000).times(1)).putFile(
                eq(TEST_COLLECTION_ID), eq(firstFile.getLocalUrl()), eq(firstFile.getFileID()), eq(0L),
                eq(checksum), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        Thread.sleep(1000);
        assertFalse(runner.finished);

        CompleteEvent firstFileComplete = new CompleteEvent(TEST_COLLECTION_ID, null);
        firstFileComplete.setFileID(firstFile.getFileID());
        eventHandlerCaptor.getValue().handleEvent(firstFileComplete);
        Thread.sleep(2000);
        assertTrue(runner.finished);
    }
    
    /**
     * Tests that the ingester retries in the event that the putfile failes 
     * @throws MalformedURLException 
     * @throws InterruptedException 
     */
    @Test
    public void PutFileFailureRetryTest() throws MalformedURLException, InterruptedException {
        putFileClient = mock(PutFileClient.class);
        int maxNumberOfParallelPuts = 1;
        ChecksumDataForFileTYPE checksum = getChecksum("aa");
        operationLimiter = new ParallelOperationLimiter(maxNumberOfParallelPuts);
        treeIngester = new TreeIngester(TEST_COLLECTION_ID, operationLimiter, urlRegister, fileLocator, putFileClient, resultCollector, 
                DEFAULT_MAX_RETRIES, testBatch);
        
        IngestableFile firstFile =
                new IngestableFile("First-file", new URL("http://somewhere.someplace/first-file"), checksum, 0L,
                        "path:First-file");
        when(fileLocator.nextFile()).thenReturn(firstFile).thenReturn(null);

        //We need to run the ingest in a separate thread, as it will block.
        TreeIngestRunner runner = new TreeIngestRunner();
        Thread t = new Thread(runner);
        t.start();
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(putFileClient, timeout(3000).times(1)).putFile(
                eq(TEST_COLLECTION_ID), eq(firstFile.getLocalUrl()), eq(firstFile.getFileID()), eq(0L),
                eq(checksum), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
        Thread.sleep(1000);
        assertFalse(runner.finished);

        OperationFailedEvent failureEvent = new OperationFailedEvent(TEST_COLLECTION_ID, null, null);
        failureEvent.setFileID(firstFile.getFileID());
        eventHandlerCaptor.getValue().handleEvent(failureEvent);

        assertFalse(runner.finished);

        verify(putFileClient, timeout(3000).times(2)).putFile(eq(TEST_COLLECTION_ID), eq(firstFile.getLocalUrl()),
                                                              eq(firstFile.getFileID()), eq(0L), eq(checksum),
                                                              (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(),
                                                              (String) isNull());
        assertFalse(runner.finished);

        CompleteEvent successEvent = new CompleteEvent(TEST_COLLECTION_ID, null);
        successEvent.setFileID(firstFile.getFileID());
        eventHandlerCaptor.getValue().handleEvent(successEvent);
        Thread.sleep(2000);
        assertTrue(runner.finished);
    }
    
    /**
     * Tests that the ingester fails a putfile after a number of retries 
     * @throws InterruptedException 
     * @throws MalformedURLException 
     */
    @Test
    public void PutFileTooManyRetriesTest() throws InterruptedException, MalformedURLException {
        putFileClient = mock(PutFileClient.class);
        int maxNumberOfParallelPuts = 1;
        ChecksumDataForFileTYPE checksum = getChecksum("aa");
        operationLimiter = new ParallelOperationLimiter(maxNumberOfParallelPuts);
        treeIngester = new TreeIngester(TEST_COLLECTION_ID, operationLimiter, urlRegister, fileLocator, putFileClient, resultCollector, 
                DEFAULT_MAX_RETRIES, testBatch);
        
        IngestableFile firstFile =
                new IngestableFile("retry-failure-file", new URL("http://somewhere.someplace/retry-failure-file"), checksum, 0L,
                        "path:retry-failure-file");
        when(fileLocator.nextFile()).thenReturn(firstFile).thenReturn(null);

        //We need to run the ingest in a separate thread, as it will block.
        TreeIngestRunner runner = new TreeIngestRunner();
        Thread t = new Thread(runner);
        t.start();
        
        OperationFailedEvent failureEvent = new OperationFailedEvent(TEST_COLLECTION_ID, null, null);
        failureEvent.setFileID(firstFile.getFileID());

        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        for(int i = 1; i<=3; i++) {
            verify(putFileClient, timeout(3000).times(i)).putFile(
                    eq(TEST_COLLECTION_ID), eq(firstFile.getLocalUrl()), eq(firstFile.getFileID()), eq(0L),
                    eq(checksum), (ChecksumSpecTYPE) isNull(), eventHandlerCaptor.capture(), (String) isNull());
            Thread.sleep(1000);
            assertFalse(runner.finished);
            
            eventHandlerCaptor.getValue().handleEvent(failureEvent);
        }
        
        Thread.sleep(3000);
        assertTrue(runner.finished);
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

    private class TreeIngestRunner implements Runnable {
        boolean finished = false;

        public void run() {
			try {
                treeIngester.performIngest();
            } catch (InterruptedException e) {
                // Err, not sure if we want to do anything?
            }
            finished = true;
        }
    }
}
