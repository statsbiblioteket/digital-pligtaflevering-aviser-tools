package dk.statsbiblioteket.newspaper.bitrepository.ingester.utils;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Used for stubbing a actual bitrepository, thereby avoiding all the complications associated with
 * testing agaist a real backend. All calls to the put file client are store in the.
 * The purpose of this class is not to have functionally god and errorconsistent code, but rather to have something that simulates the use of the ingester client.
 * <code>runningOperations</code> list.
 */
public class BitrepositoryPutFileClientStub implements PutFileClient {
    private static Logger log = LoggerFactory.getLogger(BitrepositoryPutFileClientStub.class);
    private String destinationPath;

    public BitrepositoryPutFileClientStub(String destinationPath) {
        this.destinationPath = destinationPath;
        // Fail up front if not there!
        Path absolutePath = Paths.get(destinationPath).toAbsolutePath();
        if (absolutePath.toFile().exists() == false) {
            throw new RuntimeException("destinationPath",
                    new FileNotFoundException(absolutePath.toString())
            );
        }
    }

    @Override
    public void putFile(String collectionID, URL url, String fileId, long sizeOfFile,
                        ChecksumDataForFileTYPE checksumForValidationAtPillar,
                        ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler,
                        String auditTrailInformation) {

        CompleteEvent completeEvent = new CompleteEvent(null, null);
        try {
            if(!Arrays.equals(this.getFileChecksum(MessageDigest.getInstance("md5"), url.openStream()),checksumForValidationAtPillar.getChecksumValue())) {
                completeEvent.setEventType(OperationEvent.OperationEventType.FAILED);
                completeEvent.setInfo("MD5 check has failed on the file : " + url);
            }
            String newFileId = this.destinationPath + File.separator + fileId;
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(newFileId);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            completeEvent.setFileID(fileId);
            completeEvent.setOperationType(OperationType.PUT_FILE);
            eventHandler.handleEvent(completeEvent);
        } catch(Exception e) {
            completeEvent.setEventType(OperationEvent.OperationEventType.FAILED);
            completeEvent.setInfo(e.getMessage());
            log.error("Ingester simulator failed :" + e.getMessage());
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
