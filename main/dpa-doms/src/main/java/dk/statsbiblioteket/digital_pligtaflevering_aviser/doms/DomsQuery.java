package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryQuery;
import dk.statsbiblioteket.medieplatform.autonomous.*;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class DomsQuery<I extends Item> implements RepositoryQuery<QuerySpecification, Stream<DomsEventAdder>> {

    private DomsEventStorage<I> domsEventStorage;
    private SBOIEventIndex<I> sboiEventIndex;

    public DomsQuery(DomsEventStorage<I> domsEventStorage, SBOIEventIndex<I> sboiEventIndex) {
        this.domsEventStorage = domsEventStorage;
        this.sboiEventIndex = sboiEventIndex;
    }

    @Override
    public Stream<DomsEventAdder> query(QuerySpecification querySpecification) {

        EventTrigger.Query<I> query = new EventTrigger.Query<>();

        query.getPastSuccessfulEvents().addAll(querySpecification.getPastSuccessfulEvents());
        query.getFutureEvents().addAll(querySpecification.getFutureEvents());
        query.getOldEvents().addAll(querySpecification.getOldEvents());
        query.getTypes().addAll(querySpecification.getTypes());

        // currently only ask once and stop.

        boolean details = querySpecification.getDetails();

        try {
            // http://stackoverflow.com/a/24511534/53897

            Iterator<I> searchIterator = sboiEventIndex.search(details, query);
            Iterable<I> iterable = () -> searchIterator;
            Stream<I> targetStream = StreamSupport.stream(iterable.spliterator(), false);

            Function<I, DomsItem> function = item -> new DomsItem(item, domsEventStorage);
            return targetStream.map(function);
        } catch (CommunicationException e) {
            // well?
            throw new RuntimeException("no clue how to handle", e);
        }

    }
}
