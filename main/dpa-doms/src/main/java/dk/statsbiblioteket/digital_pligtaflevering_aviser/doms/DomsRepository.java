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
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulator;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import javaslang.control.Try;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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

    public WebResource getWebResource() {
        return webResource;
    }

    @Inject
    public DomsRepository(SBOIEventIndex<Item> sboiEventIndex, WebResource webResource, EnhancedFedora efedora, DomsEventStorage<Item> domsEventStorage) {
        this.sboiEventIndex = sboiEventIndex;
        this.webResource = webResource;
        this.efedora = efedora;
        this.domsEventStorage = domsEventStorage;
    }

    @Override
    public Stream<DomsItem> query(QuerySpecification querySpecification) {

        // -- Create and populate SBIO query and return the DOMS ids found as a stream.

        EventTrigger.Query<Item> eventTriggerQuery = new EventTrigger.Query<>();

        eventTriggerQuery.getPastSuccessfulEvents().addAll(querySpecification.getPastSuccessfulEvents());
        eventTriggerQuery.getFutureEvents().addAll(querySpecification.getFutureEvents());
        eventTriggerQuery.getOldEvents().addAll(querySpecification.getOldEvents());
        eventTriggerQuery.getTypes().addAll(querySpecification.getTypes());

        boolean details = querySpecification.getDetails();

        try {
            // To keep it simple, read in the whole response as a list and create the stream from that.

            List<DomsItem> domsItemList = new ArrayList<>();

            Iterator<Item> searchIterator = sboiEventIndex.search(details, eventTriggerQuery);
            searchIterator.forEachRemaining(item -> domsItemList.add(new DomsItem(new DomsId(item.getDomsID()), this)));

            return domsItemList.stream();

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

    /**
     * appendEventToItem provides a link to DomsEventStorage.appendEventToItem(...) using a DomsId.
     *
     * @param domsId    domsId to add event to
     * @param agent     text string identifying this autonomous component
     * @param timestamp timestamp being put into the event, usually "now".
     * @param details   humanly readable string describing this event
     * @param eventType String identifying what event this is, examples  "Data_Archived", "Data_Received"
     * @param outcome   true=success, false=failure.
     * @return
     */

    public Date appendEventToItem(DomsId domsId, String agent, Date timestamp, String details, String eventType, boolean outcome) {
        Item fakeItemToGetThroughAPI = new Item(domsId.id()); //
        try {
            return domsEventStorage.appendEventToItem(fakeItemToGetThroughAPI, agent, timestamp, details, eventType, outcome);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("appendEventToItem failed for " + domsId, e);
        }
    }

    /**
     * removeEventsFromItem removes all events of the given eventType from the given item.
     *
     * @param domsId    domsId to add event to
     * @param eventType String identifying what event this is, examples  "Data_Archived", "Data_Received"
     * @return number removed
     */

    public int removeEventsFromItem(DomsId domsId, String eventType) {
        Item fakeItemToGetThroughAPI = new Item(domsId.id()); //
        try {
            return domsEventStorage.removeEventFromItem(fakeItemToGetThroughAPI, eventType);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("removeEventsFromItem failed for " + domsId, e);
        }
    }

    /**
     * Returns the contents of the given datastream as a String (as returned by Jersey <code>get(String.class)</code>)
     */

    public String getDataStreamAsString(String pid, String datastreamName) {
        try {
            String result = efedora.getXMLDatastreamContents(pid, datastreamName, new Date().getTime());
            return result;
        } catch (BackendInvalidCredsException | BackendInvalidResourceException | BackendMethodFailedException e) {
            throw new RuntimeException("cannot load pid " + pid + " datastream " + datastreamName, e);
        }
    }

    /**
     * get the content of the DC datastream on XML form parsed into a W3C DOM structur so we can post-process it easily.
     * EnhancedFedora does not expose the "get DC explicitly as xml" functionality directly.  Note that
     * we work with Strings, which may result in encoding problems if ever the response includes non-ASCII characters!
     *
     * @param domsId id of doms object to retrieve DC datastream from.
     * @return DC on XML form.
     */
    public String getDC(DomsId domsId) {
        final String id = domsId.id();
        final String p = "/datastreams/DC/content";

        String dcContent = webResource.path(id).path(p).queryParam("format", "xml").get(String.class);
        return dcContent;
    }

    /**
     * We need the premis object for the event stream.  Unfortunately the original DomsEventStorage.getPremisForItem()
     * method is private, so we need a copy here (adapted for try)
     * @param id
     * @param eventDataStreamName
     * @return
     */
    public PremisManipulator<Item> getPremisFor(String id, String eventDataStreamName) {
        Objects.requireNonNull(id, "id==null");
        String premisPreBlob = getDataStreamAsString(id, eventDataStreamName);
        PremisManipulatorFactory<Item> factory = Try.of(() -> new PremisManipulatorFactory(eventDataStreamName, i -> new Item(i))).get();

        PremisManipulator<Item> premisObject = Try.of(
                () -> factory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()))
        ).getOrElse(Try.of(
                () -> factory.createInitialPremisBlob(/* we don't have item.getFullID() */ id)
                ).get()
        );
        return premisObject;
    }

    @Override
    public void close() throws Exception {
        // nothing yet.
    }
}

