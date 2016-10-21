package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

/**
 *
 */
public class InfomediaBatch extends Item {
    private final ObjectProfile objectProfile;

    public InfomediaBatch(String domsID, ObjectProfile objectProfile) {
        super(domsID);
        this.objectProfile = objectProfile;
    }
}
