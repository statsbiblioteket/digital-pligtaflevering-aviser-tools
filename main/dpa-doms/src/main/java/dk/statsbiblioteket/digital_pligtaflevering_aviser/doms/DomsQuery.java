package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryQuery;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.apache.solr.client.solrj.SolrServerException;

import java.net.ConnectException;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 * @noinspection WeakerAccess
 */
public class DomsQuery implements RepositoryQuery<QuerySpecification, Stream<DomsId>> {

    private DomsEventStorage<Item> domsEventStorage;
    private SBOIEventIndex<Item> sboiEventIndex;
    private EnhancedFedora efedora;

    public DomsQuery(DomsEventStorage<Item> domsEventStorage, SBOIEventIndex<Item> sboiEventIndex, EnhancedFedora efedora) {
        this.domsEventStorage = domsEventStorage;
        this.sboiEventIndex = sboiEventIndex;
        this.efedora = efedora;
    }

    @Override
    public Stream<DomsId> query(QuerySpecification querySpecification) {

        // -- Create and populate SBIO query and return the DOMS ids found as a stream.

        EventTrigger.Query<Item> query = new EventTrigger.Query<>();

        query.getPastSuccessfulEvents().addAll(querySpecification.getPastSuccessfulEvents());
        query.getFutureEvents().addAll(querySpecification.getFutureEvents());
        query.getOldEvents().addAll(querySpecification.getOldEvents());
        query.getTypes().addAll(querySpecification.getTypes());

        boolean details = querySpecification.getDetails();


        try {
            // Convert Iterator<Item> to Stream<Item> - http://stackoverflow.com/a/24511534/53897

            Iterator<Item> searchIterator = sboiEventIndex.search(details, query);
            Iterable<Item> iterable = () -> searchIterator;
            Stream<Item> targetStream = StreamSupport.stream(iterable.spliterator(), false);

            Function<Item, DomsId> function = item -> new DomsId(item.getDomsID());
            return targetStream.map(function);
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
            throw new RuntimeException("no clue how to handle", e);
        }

    }
}
