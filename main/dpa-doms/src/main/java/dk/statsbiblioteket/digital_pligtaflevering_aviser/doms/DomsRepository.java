package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Repository;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class DomsRepository implements Repository<DomsId, DomsEvent, QuerySpecification, DomsItem> {
    private SBOIEventIndex<Item> sboiEventIndex;
    private WebResource webResource;
    private EnhancedFedora efedora;
    private DomsEventStorage<Item> domsEventStorage;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DomsRepository(SBOIEventIndex<Item> sboiEventIndex, WebResource webResource, EnhancedFedora efedora, DomsEventStorage<Item> domsEventStorage) {
        this.sboiEventIndex = sboiEventIndex;
        this.webResource = webResource;
        this.efedora = efedora;
        this.domsEventStorage = domsEventStorage;
    }

    @Override
    public Stream<DomsId> query(QuerySpecification querySpecification) {

        // -- Create and populate SBIO query and return the DOMS ids found as a stream.

        EventTrigger.Query<Item> eventTriggerQuery = new EventTrigger.Query<>();

        eventTriggerQuery.getPastSuccessfulEvents().addAll(querySpecification.getPastSuccessfulEvents());
        eventTriggerQuery.getFutureEvents().addAll(querySpecification.getFutureEvents());
        eventTriggerQuery.getOldEvents().addAll(querySpecification.getOldEvents());
        eventTriggerQuery.getTypes().addAll(querySpecification.getTypes());

        boolean details = querySpecification.getDetails();

        try {
            // To keep it simple, read in the whole response as a list and create the stream from that.

            List<DomsId> domsIdList = new ArrayList<>();

            Iterator<Item> searchIterator = sboiEventIndex.search(details, eventTriggerQuery);
            searchIterator.forEachRemaining(item -> domsIdList.add(new DomsId(item.getDomsID())));

            return domsIdList.stream();

        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SolrServerException) {
                if (((SolrServerException) cause).getRootCause() instanceof ConnectException) {
                    // No DOMS running.  Consider what to do.
                    System.err.println("NO DOMS RUNNING...");
                }
            }
            throw e;
        } catch (CommunicationException e) {
            // well?
            throw new RuntimeException("failed query for " + querySpecification, e);
        }
    }

    /**
     * Logic lifted from https://github.com/statsbiblioteket/newspaper-batch-event-framework/blob/master/newspaper-batch-event-framework/tree-processor/src/main/java/dk/statsbiblioteket/medieplatform/autonomous/iterator/fedora3/IteratorForFedora3.java#L146
     *
     * @param parent
     * @return
     */
    public List<DomsId> childrenFor(DomsId parent) {
        log.trace("childrenFor: {}", parent);
        final WebResource wr;
        wr = webResource.path(parent.id()).path("relationships").queryParam("format", "ntriples");
        log.trace("WebResource: {}", wr.getURI());

        //<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/relations-external#hasPart> <info:fedora/uuid:5cfa5459-c553-4894-980e-b2b7b37b37d3> .
        //<info:fedora/uuid:e1213a2b-909e-4ed7-a3ca-7eca1fe35688> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/doms:ContentModel_DOMS> .
        String rawResult = wr.get(String.class);

        List<DomsId> children = new ArrayList<>();
        for (String line : rawResult.split("\n")) {
            String[] tuple = line.split(" ");
            if (tuple.length >= 3 && tuple[2].startsWith("<info:fedora/")) {
                String predicate = tuple[1].substring(1, tuple[1].length() - ">".length());
                String child = tuple[2].substring("<info:fedora/".length(), tuple[2].length() - ">".length());

                if (predicate.equals("info:fedora/fedora-system:def/relations-external#hasPart")) {
                    children.add(new DomsId(child));
                }
            }
        }
        return children;
    }

    public List<DomsId> allChildrenFor(DomsId root) {
        log.trace("allChildrenFor: {}", root);
        List<DomsId> allChildrenSoFar = new ArrayList<>();
        List<DomsId> unprocessed = new ArrayList<>();

        unprocessed.add(root);
        while (unprocessed.isEmpty() == false) {
            DomsId currentId = unprocessed.remove(0);
            allChildrenSoFar.add(currentId);

            List<DomsId> currentChildren = childrenFor(currentId);
            for (DomsId child : currentChildren) {
                if (allChildrenSoFar.contains(child)) {
                    // seen before, malformed tree, shouldn't happen
                    log.warn("DomsID {} have {} as multiple children", root, child);
                } else {
                    unprocessed.add(child);
                }
            }
        }
        return allChildrenSoFar;
    }

    @Override
    public DomsItem lookup(DomsId id) {
        DomsItem domsItem = new DomsItem(id, this);
        return domsItem;
    }

    public ObjectProfile getObjectProfile(String id, Long asOfTime) {
        try {
            ObjectProfile objectProfile = efedora.getObjectProfile(id, asOfTime);
            return objectProfile;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
            throw new RuntimeException("could not getObjectProfile() for domsId " + id, e);
        }
    }

    public Date modifyDatastreamByValue(DomsId domsId, String datastream, ChecksumType checksumType, String checksum, byte[] contents, List<String> alternativeIdentifiers, String mimetype, String comment, Long lastModifiedDate) {
        try {
            return efedora.modifyDatastreamByValue(domsId.id(), datastream, checksumType, checksum, contents, alternativeIdentifiers, mimetype, comment, lastModifiedDate);
        } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
            throw new RuntimeException("could not save datastream " + datastream + " for id " + domsId);
        }
    }

    public Date appendEventToItem(DomsId domsId, String agent, Date timestamp, String details, String eventType, boolean outcome) {
        Item fakeItemToGetThroughAPI = new Item(domsId.id());
        try {
            return domsEventStorage.appendEventToItem(fakeItemToGetThroughAPI, agent, timestamp, details, eventType, outcome);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("appendEventToItem failed for " + domsId, e);        }
    }
}

