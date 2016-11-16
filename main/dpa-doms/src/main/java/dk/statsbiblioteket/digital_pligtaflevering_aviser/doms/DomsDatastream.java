package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;

/**
 * A DomsDatastream is a thin "expose only the needed methods" wrapper around the DOMS specific
 * DatastreamProfile, to avoid leakage.
 */
public class DomsDatastream {
    private DatastreamProfile datastreamProfile;

    public DomsDatastream(DatastreamProfile datastreamProfile) {
        this.datastreamProfile = datastreamProfile;
    }

    public String getMimeType() {
        return datastreamProfile.getMimeType();
    }

    public String getUrl() {
        return datastreamProfile.getUrl();
    }
}
