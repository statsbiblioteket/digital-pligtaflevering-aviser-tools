package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

import java.util.stream.Stream;

/**
 *
 */
public interface Edition {
    Stream<SinglePage> getPages();
}
