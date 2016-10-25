package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Repository;

import java.util.stream.Stream;

/**
 *
 */
public class DomsRepository implements Repository<DomsId, DomsEvent, DomsQuery> {
    @Override
    public Stream<DomsId> query(DomsQuery query) {
        return Stream.of();  // For now.
    }
}
