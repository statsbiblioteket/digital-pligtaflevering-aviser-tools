package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.stream.Stream;

/**
 * A Repository contains Items which can be Queried for and have Events added.
 */
public interface Repository<I extends Id, E extends Event, Q> extends //
        RepositoryQuery<Q, Stream<I>> {

    /** FIXME: put operation is needed to add veraPDF output.  Precise semantics pending.*/
    // Map<Id, I> items();
}
