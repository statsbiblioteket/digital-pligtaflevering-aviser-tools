package dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.OperationType;

/**
 * Used for stubbing a actual bitrepository, thereby avoiding all the complications associated with
 * testing agaist a real backend. All calls to the put file client are store in the.
 * The purpose of this class is not to have functionally god and errorconsistent code, but rather to have something that simulates the use of the ingester client.
 * <code>runningOperations</code> list.
 */
public class MyPutFileClientStub implements PutFileClient {
    public List<ActivePutOperation> runningOperations = new ArrayList<ActivePutOperation>();

    @Override
    public void putFile(String collectionID, URL url, String fileId, long sizeOfFile,
                        ChecksumDataForFileTYPE checksumForValidationAtPillar,
                        ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler,
                        String auditTrailInformation) {

        CompleteEvent completeEvent = new CompleteEvent(null, null);
        runningOperations.add(new ActivePutOperation(collectionID, url, fileId, sizeOfFile,
                checksumForValidationAtPillar, checksumRequestsForValidation, eventHandler, auditTrailInformation));
        try {
            if(!Arrays.equals(this.getFileChecksum(MessageDigest.getInstance("md5"), url.openStream()),checksumForValidationAtPillar.getChecksumValue())) {
                completeEvent.setEventType(OperationEvent.OperationEventType.FAILED);
            }
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream("FETCHED-" + fileId+".pdf");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            completeEvent.setFileID(fileId);
            completeEvent.setOperationType(OperationType.PUT_FILE);
            eventHandler.handleEvent(completeEvent);
        } catch(Exception e) {
            completeEvent.setEventType(OperationEvent.OperationEventType.FAILED);
            e.printStackTrace();
        }
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


    /**
     * Get the md5 checksum of a file
     * @param digest Digester (i.e. md5)
     * @param fis  inputstream to digest
     * @return checksum
     * @throws IOException
     */
    private byte[] getFileChecksum(MessageDigest digest, InputStream fis) throws IOException {

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes and return it
        return digest.digest();
    }
}