package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Repository;

import java.util.stream.Stream;

/**
 *
 */
public class DomsRepository implements Repository<DomsEventAdder, DomsEvent, DomsQuery> {
    @Override
    public DomsEvent put(DomsEventAdder item, DomsEvent addValue) {
        return null;
    }

    @Override
    public Stream<DomsEventAdder> query(DomsQuery query) {
        return null;
    }
}
