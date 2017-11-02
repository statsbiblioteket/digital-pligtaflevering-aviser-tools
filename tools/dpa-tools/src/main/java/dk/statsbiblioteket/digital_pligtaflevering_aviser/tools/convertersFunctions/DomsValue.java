package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;

/**
 *
 */
public class DomsValue<V> extends IdValue<DomsId, V> {
    public DomsValue(DomsId id, V value) {
        super(id, value);
    }
}
