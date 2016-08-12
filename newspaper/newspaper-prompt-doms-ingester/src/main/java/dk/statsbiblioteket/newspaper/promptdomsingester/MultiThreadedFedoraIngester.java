package dk.statsbiblioteket.newspaper.promptdomsingester;

import com.google.common.io.CharStreams;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.AddRelationsRequest;
import dk.statsbiblioteket.newspaper.promptdomsingester.util.UniqueRelationsCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * This is the multithreaded fedora ingester
 * The primary work is done in the
 * compute() method. This is where the iterator is iterated, and the events is handled.
 * To start the ingester, use the public constructur, and call the ingest(iterator) method. This method will start
 * the ingest and spawn new threads as needed.
 *
 * Each nodeBegin event result in a new Ingester being created. These ingesters are submittet to a threadpool.
 * In the nodeEnd event, the ingester will join any children ingesters, in order to get the PIDs for creating
 * relations.
 */
public class MultiThreadedFedoraIngester extends RecursiveTask<String> implements IngesterInterface {

    private static final String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    private final EnhancedFedora fedora;
    private final String[] collections;
    private final int concurrency;
    private final List<ForkJoinTask<String>> childTasks = new ArrayList<>();
    private TreeIterator iterator;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String myPid;

    /**
     * Create a new fedora ingester
     * @param fedora
     * @param collections
     */
    public MultiThreadedFedoraIngester(EnhancedFedora fedora, String[] collections, int concurrency) {
        this.fedora = fedora;
        this.collections = collections;
        this.concurrency = concurrency;
    }

    /**
     * secret constructor used by the multi threaded framework to spawn new tasks
     *  @param fedora
     * @param iterator
     * @param collections
     */
    protected MultiThreadedFedoraIngester(EnhancedFedora fedora, TreeIterator iterator, String[] collections,
                                          int concurrency) {
        this(fedora, collections, concurrency);
        this.iterator = iterator;
    }

    /**
     * Compute the datastream name from a attributename
     *
     * @param attributeName the name from the attribute event
     *
     * @return the datastream name
     * @throws DomsIngesterException
     */
    private static String getDatastreamName(String attributeName) throws DomsIngesterException {
        String[] splitName = attributeName.split("\\.");
        if (splitName.length < 2) {
            throw new DomsIngesterException("Cannot find datastream name in " + attributeName);
        }
        return splitName[splitName.length - 2].toUpperCase();
    }

    /**
     * This method is the gut of the ingester. It will iterate the iterator. The first node begins event will create
     * a new object, and any further will spawn child tasks.
     * The attribute events (after the first node begins, before any other node begins) will create datastreams in
     * the object
     * The node end will cause the compute method to join the child tasks, and create hasPart relations to all of them
     *
     * @return the pid of the object created
     */
    @Override
    protected String compute() {
        boolean firstEvent = true;
        try {
            while (iterator.hasNext()) {
                ParsingEvent event = iterator.next();
                switch (event.getType()) {
                    case NodeBegin:
                        if (firstEvent) {//The first node begins create this object
                            myPid = handleNodeBegin((NodeBeginsParsingEvent) event);
                            firstEvent = false;
                        } else { //any further will spawn sub iterators
                            //Skip to next sibling will branch of the iterator that began with this node begins
                            //It will then return than iterator.
                            //And the iterator where this was called will skip to the next node begins that was not this tree
                            TreeIterator childIterator = iterator.skipToNextSibling();
                            MultiThreadedFedoraIngester childIngester = new MultiThreadedFedoraIngester(
                                    fedora, childIterator, collections, concurrency);
                            childTasks.add(childIngester.fork());
                        }
                        break;
                    case Attribute:
                        handleAttribute((AttributeParsingEvent) event);
                        break;
                    case NodeEnd:
                        handleNodeEnd();
                        break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return myPid;
    }

    /**
     * Create a new object from the event
     *
     * @param event the node begins event
     *
     * @return the doms pid
     * @throws BackendInvalidCredsException
     * @throws BackendMethodFailedException
     * @throws PIDGeneratorException
     */
    private String handleNodeBegin(NodeBeginsParsingEvent event) throws
                                                                 BackendInvalidCredsException,
                                                                 BackendMethodFailedException,
                                                                 PIDGeneratorException {
        String id = getDCidentifier(event);
        String currentNodePid = exists(event);
        if (currentNodePid == null) {
            ArrayList<String> oldIds = new ArrayList<>();
            oldIds.add(id);
            String logMessage = "Created object with DC id " + id;
            currentNodePid = fedora.newEmptyObject(oldIds, getCollections(), logMessage);
            log.debug("{}" + logMessage + " / " + currentNodePid, currentNodePid);
        }
        return currentNodePid;
    }

    /**
     * Wait for all the childPid tasks to complete, and then create the relations
     *
     * @throws BackendMethodFailedException
     * @throws BackendInvalidResourceException
     * @throws BackendInvalidCredsException
     */
    private void handleNodeEnd() throws
                                 BackendMethodFailedException,
                                 BackendInvalidResourceException,
                                 BackendInvalidCredsException {
        ArrayList<String> childRealPids = new ArrayList<>();
            for (ForkJoinTask<String> childPid : childTasks) {
                childRealPids.add(childPid.join());
            }
            String comment = "Added relationship from " + myPid + " hasPart to " + childRealPids.size() + " children";
            AddRelationsRequest addRelationsRequest = new AddRelationsRequest();
            addRelationsRequest.setPid(myPid);
            addRelationsRequest.setSubject(null);
            addRelationsRequest.setPredicate(hasPartRelation);
            addRelationsRequest.setObjects(childRealPids);
            addRelationsRequest.setComment("Modified by " + getClass().getSimpleName());
            UniqueRelationsCreator uniqueRelationsCreator = new UniqueRelationsCreator(fedora);
            uniqueRelationsCreator.addRelationships(addRelationsRequest);
            //fedora.addRelations(myPid, null, hasPartRelation, childRealPids, false, comment);
            log.debug("{}, " + comment, myPid);

    }

    /**
     * Create the datastream from the attribute event
     *
     * @param event the event
     *
     * @throws DomsIngesterException
     * @throws BackendInvalidCredsException
     * @throws BackendMethodFailedException
     * @throws BackendInvalidResourceException
     */
    private void handleAttribute(AttributeParsingEvent event) throws
                                                              DomsIngesterException,
                                                              BackendInvalidCredsException,
                                                              BackendMethodFailedException,
                                                              BackendInvalidResourceException {
        if (event.getName().endsWith("/contents")) {
            //Possibly check that you are in a DataFileDir before ignoring the myObjectEvent?
            log.debug("{}, Skipping contents attribute.", myPid);
        } else {
            String comment = "Adding datastream for " + event.getName() + " == " + myPid;
            List<String> alternativeIdentifiers = new ArrayList<>();
            alternativeIdentifiers.add(event.getName());
            log.debug("{}," + comment, myPid);
            String datastreamName = getDatastreamName(event.getName());
            log.debug("{}, Ingesting datastream '" + datastreamName + "'", myPid);
            String metadataText;
            try {
                metadataText = CharStreams.toString(new InputStreamReader(event.getData(), "UTF-8"));
            } catch (IOException e) {
                throw new DomsIngesterException(e);
            }
            String checksum = null;
            try {
                checksum = event.getChecksum().toLowerCase();
            } catch (IOException e) {
                throw new DomsIngesterException(e);
            }
            fedora.modifyDatastreamByValue(myPid,
                                                  datastreamName,
                                                  ChecksumType.MD5,
                                                  checksum,
                                                  metadataText.getBytes(),
                                                  alternativeIdentifiers,
                                                  "text/xml",
                                                  "Added by ingester.",
                                                  null);
        }
    }

    /**
     * Check if the event correspond to an object that already exists
     *
     * @param nodeBeginsParsingEvent the event to look up
     *
     * @return the pid or null if not found
     * @throws BackendInvalidCredsException
     * @throws BackendMethodFailedException
     */
    private String exists(NodeBeginsParsingEvent nodeBeginsParsingEvent) throws
                                                                         BackendInvalidCredsException,
                                                                         BackendMethodFailedException {
        List<String> founds = fedora.findObjectFromDCIdentifier(getDCidentifier(nodeBeginsParsingEvent));
        if (founds != null && founds.size() > 0) {
            return founds.get(0);
        } else {
            return null;
        }
    }

    /**
     * Convert the event to a dc identifier
     *
     * @param event the event
     *
     * @return the dc identifier
     */
    private String getDCidentifier(NodeBeginsParsingEvent event) {
        String dir = event.getName();
        return "path:" + dir;
    }

    /**
     * Returns a list of collections all new objects must belong to. May be empty.
     *
     * @return
     */
    protected List<String> getCollections() {
        return Arrays.asList(collections);
    }

    /**
     * Start the ingest procedure.
     *
     * @param iterator the iterator to iterate on
     *
     * @return the pid of the root object, or null of something odd failed
     */
    @Override
    public String ingest(TreeIterator iterator) {
        this.iterator = iterator;
        ForkJoinPool forkJoinPool = new ForkJoinPool(concurrency);
        ForkJoinTask<String> result;
        result = forkJoinPool.submit(this);
        forkJoinPool.shutdown();
        try {
            return result.get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            log.warn("Shutting down pool {}", forkJoinPool);
            result.cancel(true);
            forkJoinPool.shutdownNow();
            boolean shutdown;
            try {
                shutdown = forkJoinPool.awaitTermination(3, TimeUnit.MINUTES);
            } catch (InterruptedException e1) {
                shutdown = false;
            }
            if (!shutdown){
                log.error("Failed to shut down forkjoinpool {}",forkJoinPool);
                System.exit(1);
            }
            log.debug("Pool shot down {}", forkJoinPool);
            throw new IngesterShutdownException(e);
        }
    }


}
