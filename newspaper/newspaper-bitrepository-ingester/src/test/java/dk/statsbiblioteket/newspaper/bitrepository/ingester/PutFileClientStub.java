package dk.statsbiblioteket.newspaper.bitrepository.ingester;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.OperationType;

/**
 * Used for stubbing a actual bitrepository, thereby avoiding all the complications associated with
 * testing agaist a real backend. All calls to the put file client are store in the
 * <code>runningOperations</code> list.
 */
public class PutFileClientStub implements PutFileClient {
    public List<ActivePutOperation> runningOperations = new ArrayList<ActivePutOperation>();

    @Override
    public void putFile(String collectionID, URL url, String fileId, long sizeOfFile,
                        ChecksumDataForFileTYPE checksumForValidationAtPillar,
                        ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler,
                        String auditTrailInformation) {
        runningOperations.add(new ActivePutOperation(collectionID, url, fileId, sizeOfFile,
                checksumForValidationAtPillar, checksumRequestsForValidation, eventHandler, auditTrailInformation));
        CompleteEvent completeEvent = new CompleteEvent(null, null);
        completeEvent.setFileID(fileId);
        completeEvent.setOperationType(OperationType.PUT_FILE);
        eventHandler.handleEvent(completeEvent);
    }

    /**
     * Contains all the parameters used in a putFile call.
     */
    public class ActivePutOperation {
        String collectionID;
        URL url;
        String fileId;
        long sizeOfFile;
        ChecksumDataForFileTYPE checksumForValidationAtPillar;
        ChecksumSpecTYPE checksumRequestsForValidation;
        EventHandler eventHandler;
        String auditTrailInformation;

        ActivePutOperation(String collectionID, URL url, String fileId, long sizeOfFile, ChecksumDataForFileTYPE checksumForValidationAtPillar, ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation) {
            this.collectionID = collectionID;
            this.url = url;
            this.fileId = fileId;
            this.sizeOfFile = sizeOfFile;
            this.checksumForValidationAtPillar = checksumForValidationAtPillar;
            this.checksumRequestsForValidation = checksumRequestsForValidation;
            this.eventHandler = eventHandler;
            this.auditTrailInformation = auditTrailInformation;
        }
    }
}