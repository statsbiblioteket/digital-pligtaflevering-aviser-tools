package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public interface RepositoryItem<E extends Event> {
    /**
     *
     */
    Map<String, String /* ? */> datastreams();
    /**
     *
     */
    Collection<E> events();
}
