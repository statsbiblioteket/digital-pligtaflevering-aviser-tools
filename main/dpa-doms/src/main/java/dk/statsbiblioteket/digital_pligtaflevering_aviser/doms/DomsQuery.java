package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryQuery;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class DomsQuery<I extends Item> implements RepositoryQuery<QuerySpecification, Stream<DomsItem>> {

    private DomsEventStorage<I> domsEventStorage;
    private SBOIEventIndex<I> sboiEventIndex;
    private Function<String, InputStream> inputStreamFor;

    public DomsQuery(DomsEventStorage<I> domsEventStorage, SBOIEventIndex<I> sboiEventIndex) {
        this.domsEventStorage = domsEventStorage;
        this.sboiEventIndex = sboiEventIndex;
    }

    @Override
    public Stream<DomsItem> query(QuerySpecification querySpecification) {

        // Create and populate avisprojekt DOMS query and wrap the result as DomsItems.

        EventTrigger.Query<I> query = new EventTrigger.Query<>();

        query.getPastSuccessfulEvents().addAll(querySpecification.getPastSuccessfulEvents());
        query.getFutureEvents().addAll(querySpecification.getFutureEvents());
        query.getOldEvents().addAll(querySpecification.getOldEvents());
        query.getTypes().addAll(querySpecification.getTypes());

        // currently only ask once and stop.

        boolean details = querySpecification.getDetails();

        try {
            // Convert Iterator<I> to Stream<I> - http://stackoverflow.com/a/24511534/53897

            Iterator<I> searchIterator = sboiEventIndex.search(details, query);
            Iterable<I> iterable = () -> searchIterator;
            Stream<I> targetStream = StreamSupport.stream(iterable.spliterator(), false);

            // create a DomsItem from each item returned from DOMS.

            Function<I, DomsItem> function = item -> new DomsItem(item, domsEventStorage);
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
