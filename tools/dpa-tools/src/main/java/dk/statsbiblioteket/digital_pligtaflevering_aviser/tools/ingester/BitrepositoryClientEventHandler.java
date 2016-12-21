package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.ToolResult;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is just an empty implementation of an eventHandler, this is used for injecting into PutfileClient
 * The handler stores  the last result of injecting, this method is inly valid when running synchronus
 */
public class BitrepositoryClientEventHandler implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String RELATION_PREDICATE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasMD5";
    public static final String CONTENTS = "CONTENTS";

    private final List<String> collections;
    private final String pageObjectId;
    private final String bitmagUrl;
    private final Path relativePath;
    private final String checksum;
    private final EnhancedFedora efedora;
    private ToolResult lastToolResult;
    private final String SOFTWARE_VERSION;


    public BitrepositoryClientEventHandler(List<String> collections, String pageObjectId, String bitmagUrl, Path relativePath, String checksum, EnhancedFedora efedora, String SOFTWARE_VERSION) {
        this.collections = collections;
        this.pageObjectId = pageObjectId;
        this.bitmagUrl = bitmagUrl;
        this.relativePath = relativePath;
        this.checksum = checksum;
        this.efedora = efedora;
        this.SOFTWARE_VERSION = SOFTWARE_VERSION;
    }


    @Override
    public void handleEvent(OperationEvent event) {

        final String filepathToBitmagUrl = bitmagUrl + event.getFileID();
        final String mimetype = "application/pdf";

        // create DOMS object for the file
        String fileObjectId = lookupObjectFromDCIdentifierAndCreateItIfNeeded("path:" + relativePath.toString());

        if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {

            try {

                // save external datastream in file object.
                efedora.addExternalDatastream(fileObjectId, "CONTENTS", event.getFileID(), filepathToBitmagUrl, "application/octet-stream", mimetype, null, "Adding file after bitrepository ingest " + SOFTWARE_VERSION);
                // Add "hasPart" relation from the page object to the file object.
                efedora.addRelation(pageObjectId, pageObjectId, "info:fedora/fedora-system:def/relations-external#hasPart", fileObjectId, false, "linking file to page " + SOFTWARE_VERSION);
                // Add the checksum relation to Fedora
                efedora.addRelation(pageObjectId, "info:fedora/" + fileObjectId + "/" + CONTENTS, RELATION_PREDICATE, checksum, true, "Adding checksum after bitrepository ingest");
                lastToolResult = ToolResult.ok("CONTENT node added for PDF for " + pageObjectId);
                log.info("Completed ingest of file " + event.getFileID());

            } catch (BackendInvalidCredsException | BackendMethodFailedException | BackendInvalidResourceException e) {
                lastToolResult = ToolResult.fail("Could not process " + fileObjectId, e);
            }

        } else if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            log.info("Failed to find PutJob for file '{}' for event '{}', skipping further handling", event.getFileID(), event.getEventType());
            lastToolResult = ToolResult.fail("Could not process " + fileObjectId);
        } else {
            log.debug("Got an event that I really don't care about, event type: '{}' for fileID '{}'", event.getEventType(), event.getFileID());
        }
    }

    /**
     * Get the last ToolResult to be used for storing the event of the file
     * @return
     */
    public ToolResult getLastToolResult() {
        return lastToolResult;
    }


    /**
     * Ensure that we have a valid DOMS id for the given dcIdentifier.  If it is not found, create
     * a new empty DOMS object and use that.
     *
     * @param dcIdentifier identifier to lookup in DOMS.
     * @return an existing DOMS id.
     */
    protected String lookupObjectFromDCIdentifierAndCreateItIfNeeded(String dcIdentifier) {

        List<String> founds = lookupObjectFromDCIdentifier(dcIdentifier);

        if (founds.isEmpty()) {
            // no DOMS object present already, create one.
            String logMessage = "Created object for " + dcIdentifier;
            try {
                final String directoryObjectPid = efedora.newEmptyObject(Arrays.asList(dcIdentifier), collections, logMessage);
                log.trace(logMessage + " / " + directoryObjectPid);
                return directoryObjectPid;
            } catch (BackendInvalidCredsException | BackendMethodFailedException | PIDGeneratorException e) {
                throw new RuntimeException("newEmptyObject() dcIdentifier=" + dcIdentifier, e);
            }
        } else {
            return founds.get(0);
        }
    }

    /**
     * Lookup the object inside fedora through the fedora restinterface
     * @param dcIdentifier
     * @return
     */
    protected List<String> lookupObjectFromDCIdentifier(String dcIdentifier) {
        try {
            return efedora.findObjectFromDCIdentifier(dcIdentifier);
        } catch (BackendInvalidCredsException | BackendMethodFailedException e) {
            throw new RuntimeException("findObjectFromDCIdentifier id=" + dcIdentifier, e);
        }
    }
}
