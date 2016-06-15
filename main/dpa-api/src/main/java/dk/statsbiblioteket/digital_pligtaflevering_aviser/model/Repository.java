package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.stream.Stream;

/**
 * A Repository contains Items which can be Queried for and have Events added.
 *
 *
 */
public interface Repository<I extends EventAdder<E>, E extends Event, Q> extends //
        ItemPutter<I, E>, //
        Query<Q, Stream<I>> {

}
