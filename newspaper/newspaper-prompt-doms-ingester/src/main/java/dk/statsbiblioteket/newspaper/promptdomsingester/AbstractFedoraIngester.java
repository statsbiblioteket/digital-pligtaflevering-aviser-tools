package dk.statsbiblioteket.newspaper.promptdomsingester;

import com.google.common.io.CharStreams;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.AddRelationsRequest;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.UniqueRelationsCreator;
import dk.statsbiblioteket.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing the actual logic for traversing the directory tree and ingesting the data to
 * DOMS. Concrete implementing subclasses need only specify the logic for determining which files are
 * data/checksums, as well as providing a connection to Fedora.
 */
public abstract class AbstractFedoraIngester implements IngesterInterface {

    String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    private Logger log = LoggerFactory.getLogger(getClass());

    private static String getDatastreamName(String attributeName) throws DomsIngesterException {
        String[] splitName = attributeName.split("\\.");
        if (splitName.length < 2) {
            throw new DomsIngesterException("Cannot find datastream name in " + attributeName);
        }
        return splitName[splitName.length - 2].toUpperCase();
    }

    /**
     * Get an EnhancedFedora object for the repository in which ingest is required.
     *
     * @return the enhanced fedora.
     */
    protected abstract EnhancedFedora getEnhancedFedora();

    /**
     * Returns a list of collections all new objects must belong to. May be empty.
     *
     * @return
     */
    protected abstract List<String> getCollections();

    /**
     * The logic of this method it that it maintains a stack (pidStack) to tell it exactly where it is in the
     * directory hierarchy.
     * It also maintains a map from pid to child-pids. When a node begins event is encountered, a new object is created
     * and the pid of this object is added to the list of children of the head of the pid stack.
     * When a node ends event is encountered, the corresponding pid is taken from the map, and relations are created
     * to all the children
     *
     * @param iterator the iterator to parse from
     *
     * @return the doms pid of the root object created
     * @throws DomsIngesterException if failing to read a file or any file is encountered without a checksum
     */
    @Override
    public String ingest(TreeIterator iterator)  {
        try {
            EnhancedFedora fedora = getEnhancedFedora();
            Deque<String> pidStack = new ArrayDeque<>();
            Map<String, Pair<NodeBeginsParsingEvent, List<String>>> childOf = new HashMap<>();
            String rootPid = null;
            while (iterator.hasNext()) {
                ParsingEvent event = iterator.next();
                switch (event.getType()) {
                    case NodeBegin:
                        NodeBeginsParsingEvent nodeBeginsParsingEvent = (NodeBeginsParsingEvent) event;
                        rootPid = handleNodeBegin(fedora, pidStack, rootPid, nodeBeginsParsingEvent, childOf);
                        break;
                    case NodeEnd:
                        NodeEndParsingEvent nodeEndParsingEvent = (NodeEndParsingEvent) event;
                        handleNodeEnd(fedora, pidStack, rootPid, nodeEndParsingEvent, childOf);
                        break;
                    case Attribute:
                        AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
                        handleAttribute(fedora, pidStack, rootPid, attributeParsingEvent);
                        break;
                }
            }
            return rootPid;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private String exists(EnhancedFedora fedora, NodeBeginsParsingEvent nodeBeginsParsingEvent) throws
                                                                                                BackendInvalidCredsException,
                                                                                                BackendMethodFailedException {
        List<String> founds = fedora.findObjectFromDCIdentifier(getDCidentifier(nodeBeginsParsingEvent));
        if (founds != null && founds.size() > 0) {
            return founds.get(0);
        } else {
            return null;
        }
    }

    private void handleAttribute(EnhancedFedora fedora, Deque<String> pidStack, String rootpid,
                                 AttributeParsingEvent event) throws
                                                              DomsIngesterException,
                                                              BackendInvalidCredsException,
                                                              BackendMethodFailedException,
                                                              BackendInvalidResourceException {
        if (event.getName().endsWith("/contents")) {
            //Possibly check that you are in a DataFileDir before ignoring the event?
            log.debug("Skipping contents attribute.");
        } else {
            String comment = "Adding datastream for " + event.getName() + " == " + pidStack.peekFirst();
            List<String> alternativeIdentifiers = new ArrayList<>();
            alternativeIdentifiers.add(event.getName());
            log.debug(comment);
            String datastreamName = getDatastreamName(event.getName());
            log.debug("Ingesting datastream '" + datastreamName + "'");
            String metadataText;
            try {
                metadataText = CharStreams.toString(new InputStreamReader(event.getData(), "UTF-8"));
            } catch (IOException e) {
                throw new DomsIngesterException(e);
            }
            String checksum = null;
            try {
                checksum = event.getChecksum();
            } catch (IOException e) {
                throw new DomsIngesterException(e);
            }
            if (checksum != null) {
                fedora.modifyDatastreamByValue(
                        pidStack.peekFirst(),
                        datastreamName,
                        metadataText,
                        checksum.toLowerCase(),
                        alternativeIdentifiers,
                        "Added by ingester.");
            } else {
                fedora.modifyDatastreamByValue(
                        pidStack.peekFirst(),
                        datastreamName,
                        metadataText,
                        alternativeIdentifiers,
                        "Added by ingester.");

            }
        }
    }

    private void handleNodeEnd(EnhancedFedora fedora, Deque<String> pidStack, String rootPid, ParsingEvent event,
                               Map<String, Pair<NodeBeginsParsingEvent, List<String>>> childOf) throws
            BackendMethodFailedException,
            BackendInvalidResourceException,
            BackendInvalidCredsException {
        String currentNodePid = pidStack.removeFirst();
        if (currentNodePid != null) {
            Pair<NodeBeginsParsingEvent, List<String>> children = childOf.remove(currentNodePid);
            String comment = "Adding relationship " + currentNodePid + " hasPart (if necessary)";
            AddRelationsRequest addRelationsRequest = new AddRelationsRequest();
            addRelationsRequest.setPid(currentNodePid);
            addRelationsRequest.setSubject(null);
            addRelationsRequest.setPredicate(hasPartRelation);
            addRelationsRequest.setObjects(children.getRight());
            addRelationsRequest.setComment("Modified by AbstractFedoraIngester.");
            UniqueRelationsCreator uniqueRelationsCreator = new UniqueRelationsCreator(fedora);
            uniqueRelationsCreator.addRelationships(addRelationsRequest);
            log.debug(comment);
        }

    }

    private String handleNodeBegin(EnhancedFedora fedora, Deque<String> pidStack, String rootPid,
                                   NodeBeginsParsingEvent event,
                                   Map<String, Pair<NodeBeginsParsingEvent, List<String>>> childOf) throws
                                                                                                    BackendInvalidCredsException,
                                                                                                    BackendMethodFailedException,
                                                                                                    PIDGeneratorException,
                                                                                                    BackendInvalidResourceException {
        String id = getDCidentifier(event);
        String currentNodePid = exists(fedora, event);
        if (currentNodePid == null) {
            ArrayList<String> oldIds = new ArrayList<>();
            oldIds.add(id);
            String logMessage = "Created object with DC id " + id;
            currentNodePid = fedora.newEmptyObject(oldIds, getCollections(), logMessage);
            log.debug(logMessage + " / " + currentNodePid);
        }
        String parentPid = pidStack.peekFirst();
        if (rootPid == null) {
            rootPid = currentNodePid;
        }
        pidStack.addFirst(currentNodePid);
        childOf.put(currentNodePid, new Pair<NodeBeginsParsingEvent, List<String>>(event, new ArrayList<String>()));

        if (parentPid != null) {
            childOf.get(parentPid).getRight().add(currentNodePid);
        }

        return rootPid;
    }

    private String getDCidentifier(NodeBeginsParsingEvent event) {
        String dir = event.getName();
        return "path:" + dir;
    }

}