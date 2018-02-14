package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.streams.StreamTuple;

/**
 *
 */
public class DomsIdTuple<V> extends StreamTuple<DomsItem, V> {
    public DomsIdTuple(DomsItem domsItem, V value) {
        super(domsItem, value);
    }

    public static DomsIdTuple<DomsItem> create(DomsItem id) {
        return new DomsIdTuple<>(id, id);
    }
}
