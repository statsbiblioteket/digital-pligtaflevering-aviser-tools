package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.doms.central.connectors.fedora.structures.DatastreamProfile;

/**
 * A DomsDatastream is a thin "expose only the needed methods" wrapper around the DOMS specific
 * DatastreamProfile, to avoid leakage.
 */
public class DomsDatastream {

    private DatastreamProfile datastreamProfile;
    private DomsItem domsItem;
    private DomsRepository domsRepository;

    public DomsDatastream(DatastreamProfile datastreamProfile, DomsItem domsItem, DomsRepository domsRepository) {
        this.datastreamProfile = datastreamProfile;
        this.domsItem = domsItem;
        this.domsRepository = domsRepository;
    }

    public DomsItem getDomsItem() {
        return domsItem;
    }

    public String getMimeType() {
        return datastreamProfile.getMimeType();
    }

    public String getUrl() {
        return datastreamProfile.getUrl();
    }
    
    public String getState() {
        return datastreamProfile.getState();
    }
    
    public String getChecksum() {
        return datastreamProfile.getChecksum();
    }
    
    public String getChecksumType() {
        return datastreamProfile.getChecksumType();
    }
    
    public long getCreated() {
        return datastreamProfile.getCreated();
    }
    
    public String getFormatURI() {
        return datastreamProfile.getFormatURI();
    }
    
    public String getId() {
        return datastreamProfile.getID();
    }
    
    public String getID() {
        return datastreamProfile.getID();
    }
    
    public String getLabel() {
        return datastreamProfile.getLabel();
    }
    
    public String getDatastreamAsString() {
        return domsRepository.getDataStreamAsString(domsItem.getDomsId().id(), getId());
    }
}
