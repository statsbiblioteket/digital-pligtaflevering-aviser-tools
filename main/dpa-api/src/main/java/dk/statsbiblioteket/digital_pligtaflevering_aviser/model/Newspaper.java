package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.stream.Stream;

/**
 *
 */
public interface Newspaper {
    Stream<Edition> editionsFor(RepositoryQuery query);
}
