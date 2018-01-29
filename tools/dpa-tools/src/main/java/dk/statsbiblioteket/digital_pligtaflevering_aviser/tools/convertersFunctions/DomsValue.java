package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.streams.IdValue;

/**
 *
 */
public class DomsValue<V> extends IdValue<DomsItem, V> {
    public DomsValue(DomsItem domsItem, V value) {
        super(domsItem, value);
    }

    public static DomsValue<DomsItem> create(DomsItem id) {
        return new DomsValue<>(id, id);
    }
}
