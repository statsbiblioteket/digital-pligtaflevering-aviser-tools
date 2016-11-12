package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.stream.Stream;

/**
 * A Repository contains Items which can be Queried for and have Events added.
 */
public interface Repository<I extends Id, E extends Event, Q, R extends RepositoryItem<E>> extends //
        RepositoryQuery<Q, Stream<I>> {

    /**
     * Get the full item for a given id.
     */
    R lookup(I id);

}
