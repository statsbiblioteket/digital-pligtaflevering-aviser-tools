package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Query;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.EventTrigger;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class DomsQuery<I extends Item> implements Query<QuerySpecification, Stream<I>> {

    private SBOIEventIndex<I> sboiEventIndex;

    public DomsQuery(SBOIEventIndex<I> sboiEventIndex) {
        this.sboiEventIndex = sboiEventIndex;
    }

    @Override
    public Stream<I> query(QuerySpecification querySpecification) {

        EventTrigger.Query<I> query = new EventTrigger.Query<>();

        query.getPastSuccessfulEvents().addAll(querySpecification.getPastSuccessfulEvents());
        query.getFutureEvents().addAll(querySpecification.getFutureEvents());
        query.getOldEvents().addAll(querySpecification.getOldEvents());
        query.getTypes().addAll(querySpecification.getTypes());

        // currently only ask once and stop.

        boolean details = false; // false=summa information only, true=ask DOMS for everything.

        try {
            // http://stackoverflow.com/a/24511534/53897

            Iterator<I> searchIterator = sboiEventIndex.search(details, query);
            Iterable<I> iterable = () -> searchIterator;
            Stream<I> targetStream = StreamSupport.stream(iterable.spliterator(), false);

            return targetStream;
        } catch (CommunicationException e) {
            // well?
            throw new RuntimeException("no clue how to handle", e);
        }

    }
}
