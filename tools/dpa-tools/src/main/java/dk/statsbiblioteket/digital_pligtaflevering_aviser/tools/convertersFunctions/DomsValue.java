package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;

/**
 *
 */
public class DomsValue<V> extends ContextValue<DomsId, V> {
    public DomsValue(DomsId context, V value) {
        super(context, value);
    }
}
