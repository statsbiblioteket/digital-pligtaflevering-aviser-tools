package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.ParallelOperationLimiter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutFileEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutJob;


public class PutFileEventHandlerTest {

    public final static String TEST_FILE_ID = "file1";
    
    /**
     * Tests handler correctly registers a failed job once, and does nothing if it gets an additional failed event
     * Also that there's no interaction with the domsRegister
     */
    @Test
    public void dubbleFailureEventTest() throws MalformedURLException {
        DomsJP2FileUrlRegister domsRegister = mock(DomsJP2FileUrlRegister.class);
        BlockingQueue<PutJob> failedJobsQueue = mock(BlockingQueue.class);
        ParallelOperationLimiter putLimiter = mock(ParallelOperationLimiter.class);
        PutFileEventHandler handler = new PutFileEventHandler(putLimiter, failedJobsQueue, domsRegister);
        
        PutJob theJob = new PutJob(new IngestableFile(TEST_FILE_ID, new URL("file:/"), null, 0L, "/"));
        when(putLimiter.getJob(TEST_FILE_ID)).thenReturn(theJob).thenReturn(null);
        
        OperationFailedEvent failureEvent = new OperationFailedEvent("avis", "Failed", null);
        failureEvent.setFileID(TEST_FILE_ID); 
        handler.handleEvent(failureEvent);
        handler.handleEvent(failureEvent);
        
        InOrder order = inOrder(putLimiter, failedJobsQueue);
        
        order.verify(putLimiter, times(1)).getJob(TEST_FILE_ID);
        order.verify(failedJobsQueue, times(1)).add(eq(theJob));
        order.verify(putLimiter, times(1)).removeJob(eq(theJob));
        order.verify(putLimiter, times(1)).getJob(TEST_FILE_ID);
        verifyNoMoreInteractions(putLimiter);
        verifyNoMoreInteractions(failedJobsQueue);
        verifyNoMoreInteractions(domsRegister);
    } 
    
    /**
     * Tests that the handler correctly registers the file for doms registration 
     */
    @Test
    public void successEventTest() throws MalformedURLException {
        DomsJP2FileUrlRegister domsRegister = mock(DomsJP2FileUrlRegister.class);
        BlockingQueue<PutJob> failedJobsQueue = mock(BlockingQueue.class);
        ParallelOperationLimiter putLimiter = mock(ParallelOperationLimiter.class);
        PutFileEventHandler handler = new PutFileEventHandler(putLimiter, failedJobsQueue, domsRegister);
        
        PutJob theJob = new PutJob(new IngestableFile(TEST_FILE_ID, new URL("file:/"), null, 0L, "/"));
        when(putLimiter.getJob(TEST_FILE_ID)).thenReturn(theJob).thenReturn(null);
        
        CompleteEvent completeEvent = new CompleteEvent("avis", null);
        completeEvent.setFileID(TEST_FILE_ID);
        
        handler.handleEvent(completeEvent);
        
        InOrder order = inOrder(putLimiter, domsRegister);
        
        order.verify(putLimiter, times(1)).getJob(TEST_FILE_ID);
        order.verify(domsRegister).registerJp2File(eq(theJob));
        order.verify(putLimiter, times(1)).removeJob(eq(theJob));
        verifyNoMoreInteractions(putLimiter);
        verifyNoMoreInteractions(failedJobsQueue);
        verifyNoMoreInteractions(domsRegister);
    }
    
    
}
