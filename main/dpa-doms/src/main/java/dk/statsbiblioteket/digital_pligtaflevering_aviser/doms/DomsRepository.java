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
import dk.statsbiblioteket.medieplatform.autonomous.PassQThrough_Query;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulator;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import dk.statsbiblioteket.medieplatform.autonomous.SolrJConnector;
import javaslang.control.Try;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.AUTONOMOUS_SBOI_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_COLLECTION;

/**
 *
 */
public class DomsRepository implements Repository<DomsId, DomsEvent, QuerySpecification, DomsItem> {
    private final HttpSolrServer summaSearch;
    private final String recordBase;
    private SBOIEventIndex<Item> sboiEventIndex;
    private WebResource webResource;
    private EnhancedFedora efedora;
    private DomsEventStorage<Item> domsEventStorage;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    public WebResource getWebResource() {
        return webResource;
    }

    @Inject
    public DomsRepository(SBOIEventIndex<Item> sboiEventIndex, @Named(DomsId.DPA_WEBRESOURCE) WebResource webResource,
                          EnhancedFedora efedora, DomsEventStorage<Item> domsEventStorage,
                          @Named(AUTONOMOUS_SBOI_URL) String summaLocation,
                          @Named(DOMS_COLLECTION) String recordBase) {
        this.sboiEventIndex = sboiEventIndex;
        this.webResource = webResource;
        this.efedora = efedora;
        this.domsEventStorage = domsEventStorage;
        this.summaSearch = new SolrJConnector(summaLocation).getSolrServer();

        this.recordBase = recordBase;
    }

    @Override
    public Stream<DomsItem> query(QuerySpecification querySpecification) {

        // -- Create and populate SBIO query and return the DOMS ids found as a stream.

        final EventTrigger.Query<Item> eventTriggerQuery;
        final boolean details;

        if (querySpecification instanceof EventQuerySpecification) {
            EventQuerySpecification eventQuerySpecification = (EventQuerySpecification) querySpecification;

            eventTriggerQuery = new EventTrigger.Query<>();
            eventTriggerQuery.getPastSuccessfulEvents().addAll(eventQuerySpecification.getPastSuccessfulEvents());
            eventTriggerQuery.getFutureEvents().addAll(eventQuerySpecification.getFutureEvents());
            eventTriggerQuery.getOldEvents().addAll(eventQuerySpecification.getOldEvents());
            eventTriggerQuery.getTypes().addAll(eventQuerySpecification.getTypes());

            details = eventQuerySpecification.getDetails();
        } else if (querySpecification instanceof SBOIQuerySpecification) {
            SBOIQuerySpecification sboiQuerySpecification = (SBOIQuerySpecification) querySpecification;
            eventTriggerQuery = new PassQThrough_Query<>(sboiQuerySpecification.getQ());
            details = false;
        } else {
            throw new UnsupportedOperationException("Bad query specification instance");
        }
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
    public long count(QuerySpecification queryX) {
        if (queryX instanceof SBOIQuerySpecification == false) {
            throw new UnsupportedOperationException("bad query type");
        }
        // Emulate SBOIEventIndex construction of query string.
        SBOIQuerySpecification sboiQuerySpecification = (SBOIQuerySpecification) queryX;
        final String q = " " + ("recordBase:" + recordBase).trim() + " " + sboiQuerySpecification.getQ();

        try {
            // Adapted from SolrProxyIterator.search()
            SolrQuery query = new SolrQuery();
            query.setQuery(q);
            // no rows of data, just the meta data including the result count.
            query.setRows(0);
            query.setStart(0);
            // IMPORTANT! Only use facets if needed.
            query.set("facet", "false"); //very important. Must overwrite to false. Facets are very slow and expensive.
            query.setFields(SBOIEventIndex.UUID, SBOIEventIndex.LAST_MODIFIED);
            boolean details = false;
            if (!details) {
                query.addField(SBOIEventIndex.PREMIS_NO_DETAILS);
            }

            query.addSort(SBOIEventIndex.SORT_DATE, SolrQuery.ORDER.asc);

            QueryResponse response = summaSearch.query(query, SolrRequest.METHOD.POST);
            SolrDocumentList results = response.getResults();
            return results.getNumFound();
        } catch (SolrServerException e) {
            throw new RuntimeException("q=" + q, e);
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


//    @Deprecated
//    public Date appendEventToItem(DomsId domsId, String agent, Date timestamp, String details, String eventType, boolean outcome) {
//        Item fakeItemToGetThroughAPI = new Item(domsId.id()); //
//        try {
//            return domsEventStorage.appendEventToItem(fakeItemToGetThroughAPI, agent, timestamp, details, eventType, outcome);
//        } catch (RuntimeException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException("appendEventToItem failed for " + domsId, e);
//        }
//    }

    /**
     * appendEventToItem provides a link to DomsEventStorage.appendEventToItem(...) using a DomsId.
     *
     * @param domsId    domsId to add event to
     * @param event eventdata holder
     * @return date returned from storage
     */

    public Date appendEventToItem(DomsId domsId, DomsEvent event) {
        Item fakeItemToGetThroughAPI = new Item(domsId.id()); //
        try {
            return domsEventStorage.appendEventToItem(fakeItemToGetThroughAPI, event.getLinkingAgentIdentifierValue(),
                    event.getTimestamp(), event.getEventOutcomeDetailNote(), event.getEventType(), event.getOutcome());
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
     *
     * @param id
     * @param eventDataStreamName
     * @return
     */
    public PremisManipulator<Item> getPremisFor(String id, String eventDataStreamName) {
        Objects.requireNonNull(id, "id==null");
        String premisPreBlob = getDataStreamAsString(id, eventDataStreamName);
        PremisManipulatorFactory<Item> factory = Try.of(() -> new PremisManipulatorFactory<>(eventDataStreamName, Item::new)).get();

        PremisManipulator<Item> premisObject = Try.of(
                () -> factory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes(StandardCharsets.UTF_8)))
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

