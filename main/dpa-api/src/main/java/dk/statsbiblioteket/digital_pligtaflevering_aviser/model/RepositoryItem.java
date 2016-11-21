package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.Collection;

/**
 *
 */
public interface RepositoryItem<E extends Event> {
    /**
     *
     */
    // FIXME: Map<String, String /* ? */> datastreams();


    /**
     *
     */
    Collection<E> events();
}
