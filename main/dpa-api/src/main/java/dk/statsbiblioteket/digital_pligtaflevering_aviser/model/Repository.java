package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.stream.Stream;

/**
 * A repository must be able to put items (which are able to add events), and must be able
 * to query for items.
 */
public interface Repository<I extends EventAdder<E>, E extends Event, Q> extends //
        ItemPutter<I, E>, //
        Query<Q, Stream<I>> {

}
