package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngestableFile;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.ParallelOperationLimiter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutFileEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.PutJob;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.mockito.InOrder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class PutFileEventHandlerTest {

    public final static String TEST_FILE_ID = "file1";
    

    
    
}
