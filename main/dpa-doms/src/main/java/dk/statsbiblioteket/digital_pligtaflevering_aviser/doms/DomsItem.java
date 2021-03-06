package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulator;
import dk.statsbiblioteket.util.xml.DOM;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static antlr.build.ANTLR.root;

/**
 * @noinspection WeakerAccess
 */
public class DomsItem implements RepositoryItem<DomsEvent> {
    
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";
    public Logger log = LoggerFactory.getLogger(getClass());

    public DomsId getDomsId() {
        return domsId;
    }

    private final DomsId domsId;

    public DomsRepository getDomsRepository() {
        return domsRepository;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomsItem domsItem = (DomsItem) o;

        return domsId.equals(domsItem.domsId);

    }

    @Override
    public int hashCode() {
        return domsId.hashCode();
    }

    private final DomsRepository domsRepository;
    private ObjectProfile objectProfile;

    /**
     * A DomsItem has an id, and knows it belongs to a Repository.
     *
     * @param domsId
     * @param domsRepository
     */
    public DomsItem(DomsId domsId, DomsRepository domsRepository) {
        this.domsId = Objects.requireNonNull(domsId, "domsId");
        this.domsRepository = domsRepository;
        this.objectProfile = null;
    }

    /**
     * Retrieve a list of data streams for further processing.  Should return a Stream.
     * <p>
     * FIXME:  Currently the underlying DatastreamProfile class leaks through.  When we know what we need, hide it.
     */

    public List<DomsDatastream> datastreams() {
        reloadIfNeeded();
        return objectProfile.getDatastreams().stream()
                .map(ds -> new DomsDatastream(ds, this, domsRepository))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve the specific datastream for further processing.
     * Return null is the datastream can not be found
     * <p>
     * FIXME:  Currently the underlying DatastreamProfile class leaks through.  When we know what we need, hide it.
     */
    public DomsDatastream datastream(String datastreamId) {
        reloadIfNeeded();
        //noinspection ConstantConditions
        return objectProfile.getDatastreams().stream()
                .filter(datastream -> datastream.getID().equals(datastreamId))
                .map(datastream -> new DomsDatastream(datastream, this,domsRepository))
                .findAny().orElse(null);
    }

    /**
     * Ensure that we have a copy of the DOMS object (and cache it for later).
     * If for any reason the copy is outdated, call <code>requireReload()</code>.
     * <p>
     * The synchronization mechanism is not optimized.
     */
    protected synchronized void reloadIfNeeded() {
        if (objectProfile == null) {
            objectProfile = domsRepository.getObjectProfile(domsId.id(), null);
            Objects.requireNonNull(objectProfile, "objectProfile not set");
        }
    }

    @Override
    public Collection<DomsEvent> events() {
        throw new UnsupportedOperationException("use originalEvents() instead for now");
    }

    // FIXME:  Refactor getOriginalEvents() into events().  It was introduced to figure out what was needed to be exposed in DomsEvent.
    public List<Event> getOriginalEvents() {
        PremisManipulator<Item> premis = getPremis();
        final Item item = premis.toItem();  // we must go through an item to get to the events.

        return item.getEventList();

    }

    /**
     * method to invoke efedora.modifyDatastreamByValue - as this changes the object in DOMS, the cached copy is
     * invalidated so it will be reloaded next time it is needed.  Do _not_ use this method while iterating over the
     * data streams!  This particular method is being used because it allows to pass in a byte array needed for XML
     * documents, and to avoid having to provide the checksum (which otherwise would cause DOMS to validate the checksum
     * which we do not want).
     *
     * @param datastream             passed on
     * @param checksumType           passed on
     * @param checksum               passed on
     * @param contents               passed on
     * @param alternativeIdentifiers passed on
     * @param mimetype               passed on
     * @param comment                passed on
     * @param lastModifiedDate       passed on
     * @return Date returned from Fedora.
     */
    public Date modifyDatastreamByValue(String datastream, ChecksumType checksumType, String checksum, byte[] contents, List<String> alternativeIdentifiers, String mimetype, String comment, Long lastModifiedDate) {
        Date date = domsRepository.modifyDatastreamByValue(domsId, datastream, checksumType, checksum, contents, alternativeIdentifiers, mimetype, comment, lastModifiedDate);
        requireReload();
        return date;
    }

//    public Date appendEventToItem(String agent, Date timestamp, String details, String eventType, boolean outcome) {
//        final Date date = domsRepository.appendEventToItem(domsId, agent, timestamp, details, eventType, outcome);
//        requireReload();
//        return date;
//    }

    /**
     * Invalidate the cached DOMS object, so the next usage will reload it from DOMS.
     */
    private void requireReload() {
        objectProfile = null;
    }

    /**
     * Helper method to create a DomsItem for an id in the same repository as this item.
     */
    public DomsItem itemFor(String id) {
        return new DomsItem(new DomsId(id), domsRepository);
    }

    /**
     * append a PREMIS event on the current item
     */
    public Date appendEvent(DomsEvent event) {
        final Date date = domsRepository.appendEventToItem(domsId, event);
        requireReload();
        return date;
    }

    /**
     * delete all instances of a specific PREMIS event on the current item.  Use with care!
     * @return number of events deleted.
     */
    public int removeEvents(String eventType) {
        final int date = domsRepository.removeEventsFromItem(domsId, eventType);
        requireReload();
        return date;
    }

    /**
     * return all direct children nodes for the current node.  For now the interface is a stream, but
     * internally the whole response is built up front.
     */
    public Stream<DomsItem> allChildren() {
        List<DomsItem> allChildrenSoFar = new ArrayList<>();
        List<DomsItem> unprocessed = new ArrayList<>();

        unprocessed.add(this);
        while (unprocessed.isEmpty() == false) {
            DomsItem currentItem = unprocessed.remove(0);
            allChildrenSoFar.add(currentItem);

            Stream<DomsItem> currentChildren = currentItem.children();
            for (DomsItem child : currentChildren.collect(Collectors.toList())) {
                if (allChildrenSoFar.contains(child)) {
                    // seen before, malformed tree, shouldn't happen
                    log.warn("DomsID {} have {} as multiple children", root, child);
                } else {
                    unprocessed.add(child);
                }
            }
        }
        return allChildrenSoFar.stream();
    }

    /**
     * Return all children nodes recursively from current node
     * <p>
     * Logic lifted from https://github.com/statsbiblioteket/newspaper-batch-event-framework/blob/master/newspaper-batch-event-framework/tree-processor/src/main/java/dk/statsbiblioteket/medieplatform/autonomous/iterator/fedora3/IteratorForFedora3.java#L146
     *
     * Deprecated because ABR points out this needs to come from the datastream on the doms item.  partsForStream()
     * @return
     */
    //@Deprecated FIXME
    public Stream<DomsItem> children() {
        log.trace("childrenFor: {}", this);
        final WebResource wr;
        wr = getDomsRepository().getWebResource().path(getDomsId().id()).path("relationships").queryParam("format", "ntriples");
        log.trace("WebResource: {}", wr.getURI());

        //<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/relations-external#hasPart> <info:fedora/uuid:5cfa5459-c553-4894-980e-b2b7b37b37d3> .
        //<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_DOMS> .
        String rawResult = wr.get(String.class);

        List<DomsItem> children = new ArrayList<>();
        for (String line : rawResult.split("\n")) {
            String[] tuple = line.split(" ");
            if (tuple.length >= 3 && tuple[2].startsWith("<info:fedora/")) {
                String predicate = tuple[1].substring(1, tuple[1].length() - ">".length());
                String child = tuple[2].substring("<info:fedora/".length(), tuple[2].length() - ">".length());

                if (predicate.equals(HAS_PART)) {
                    children.add(itemFor(child));
                }
            }
        }
        return children.stream();
    }

    @Override
    public String toString() {
        return domsId.id();
    }

    public InputStream getDataStreamInputStream(String datastreamId) {
        return domsRepository.getDataStreamInputStream(domsId, datastreamId);
    }
    /**
     * <p>
     * Returns the path identifier inserted by the ingester for a given node.  Throws a runtime exception
     * if the XPath extraction failed.  Throws NoSuchElementException if no "path:" identifier is present for the
     * object.</p>
     *
     * <p>NOTE:  ABR points out that DOM.createXPathSelector(...) does much of this and might be used instead.</p>
     *
     * @return
     */
    public String getPath() {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContextImpl context = new NamespaceContextImpl();
        context.startPrefixMapping("dc", "http://purl.org/dc/elements/1.1/");
        xPath.setNamespaceContext(context);

        try(InputStream is = getDataStreamInputStream("DC")) {
            final Document dom = DOM.streamToDOM(new BufferedInputStream(is), true);

            NodeList nodeList;
            try {
                nodeList = (NodeList) xPath.compile("//dc:identifier").evaluate(dom, XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                throw new RuntimeException("//dc:identifier failed in DC for " + this, e);
            }
            List<String> textContent = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                textContent.add(nodeList.item(i).getTextContent());
            }
            Optional<String> relativeFilenameFromDublinCore = textContent.stream()
                    .filter(s -> s.startsWith("path:"))
                    .map(s -> s.substring("path:".length()))
                    .findAny();

            return relativeFilenameFromDublinCore.orElseGet(() -> "BROKEN VALUE!!");
        } catch (IOException e) {
            throw new RuntimeException("getPath", e);
        }
    }

    // BACKPORT - DomsEventStorage.getPremisForItem() is private so adapted from that.
    public PremisManipulator<Item> getPremis() {
        return domsRepository.getPremisFor(domsId.id(), DomsEventStorageFactory.EVENTS);
    }
    
    public void delete(String logmessage){
        domsRepository.deleteItem(this,logmessage);
    }
    
    public void unlinkChild(DomsItem child, String logmessage){
        domsRepository.removeRelation(this,HAS_PART,child.domsId.id(),logmessage);
    }

}
