package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @noinspection WeakerAccess
 */
public class DomsItem implements RepositoryItem<DomsEvent> {

    private final DomsId domsId;
    private final DomsRepository domsRepository;
    private ObjectProfile objectProfile;

    /**
     * A DomsItem has an id, and knows it belongs to a Repository.
     *
     * @param domsId
     * @param domsRepository
     */
    public DomsItem(DomsId domsId, DomsRepository domsRepository) {
        this.domsId = domsId;
        this.domsRepository = domsRepository;
        this.objectProfile = null;
    }

    /**
     * Retrieve a list of data streams for further processing.
     * <p>
     * FIXME:  Currently the underlying DatastreamProfile class leaks through.  When we know what we need, hide it.
     */

    public List<DomsDatastream> datastreams() {
        reloadIfNeeded();
        return objectProfile.getDatastreams().stream()
                .map(DomsDatastream::new)
                .collect(Collectors.toList());
    }

    /**
     * Ensure that we have a copy of the DOMS object (and cache it for later).
     * If for any reason the copy is outdated, call <code>requireReload()</code>.
     * <p>
     * The synchronization mechanism is not optimized.
     */
    protected synchronized void reloadIfNeeded() {
        if (objectProfile == null) {
            objectProfile = domsRepository.getObjectProfile(domsId.id(), null);
            Objects.requireNonNull(objectProfile, "objectProfile not set");
        }
    }

    @Override
    public Collection<DomsEvent> events() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * method to invoke efedora.modifyDatastreamByValue - as this changes the object in DOMS, the cached copy is
     * invalidated so it will be reloaded next time it is needed.  Do _not_ use this method while iterating over the
     * data streams!  This particular method is being used because it allows to pass in a byte array needed for XML
     * documents, and to avoid having to provide the checksum (which otherwise would cause DOMS to validate the checksum
     * which we do not want).
     *
     * @param datastream             passed on
     * @param checksumType           passed on
     * @param checksum               passed on
     * @param contents               passed on
     * @param alternativeIdentifiers passed on
     * @param mimetype               passed on
     * @param comment                passed on
     * @param lastModifiedDate       passed on
     * @return Date returned from Fedora.
     */
    public Date modifyDatastreamByValue(String datastream, ChecksumType checksumType, String checksum, byte[] contents, List<String> alternativeIdentifiers, String mimetype, String comment, Long lastModifiedDate) {
        Date date = domsRepository.modifyDatastreamByValue(domsId, datastream, checksumType, checksum, contents, alternativeIdentifiers, mimetype, comment, lastModifiedDate);
        requireReload();
        return date;
    }

//    public Date appendEventToItem(String agent, Date timestamp, String details, String eventType, boolean outcome) {
//        final Date date = domsRepository.appendEventToItem(domsId, agent, timestamp, details, eventType, outcome);
//        requireReload();
//        return date;
//    }

    /**
     * Invalidate the cached DOMS object, so the next usage will reload it from DOMS.
     */
    private void requireReload() {
        objectProfile = null;
    }
}
