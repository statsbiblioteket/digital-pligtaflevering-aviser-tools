package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;

import java.util.stream.Stream;

/**
 *
 */
public class InfomediaBatch extends DomsItem {
    public final ObjectProfile objectProfile;
    private DomsId domsId;
    public EnhancedFedora efedora;

    public InfomediaBatch(DomsId domsId, EnhancedFedora efedora) {
        this.domsId = domsId;
        this.efedora = efedora;
        try {
            this.objectProfile = efedora.getObjectProfile(domsId.id(), null);
        } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
            throw new RuntimeException(e);
        }
    }

    public Stream<InfomediaSingleDayNewspaper> getSingleDayNewspaperStream() {
        return Stream.of(new InfomediaSingleDayNewspaper("1"), new InfomediaSingleDayNewspaper("2"));
    }
}
