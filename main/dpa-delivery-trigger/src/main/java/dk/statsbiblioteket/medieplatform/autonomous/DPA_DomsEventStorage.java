package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.triggers.Delivery;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import javax.xml.bind.JAXBException;
import java.util.List;

/**
 * MUST be in dk.statsbiblioteket.medieplatform.autonomous to reach default scoped super-constructor.
 */
public class DPA_DomsEventStorage extends DomsEventStorage {
    DPA_DomsEventStorage(EnhancedFedora fedora, String type, String eventsDatastream, ItemFactory itemFactory) throws JAXBException {
        super(fedora, type, eventsDatastream, itemFactory);
    }

    public List<Delivery> getAllRoundTrips(Delivery batchID) {
        return null;
    }
}
