package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

/**
 *
 */
public class DomsId implements Id {

    private String id;

    public DomsId(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
