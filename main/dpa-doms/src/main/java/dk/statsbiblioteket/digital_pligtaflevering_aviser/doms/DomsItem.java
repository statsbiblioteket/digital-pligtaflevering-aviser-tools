package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *
 * @noinspection WeakerAccess
 */
public class DomsItem implements RepositoryItem<DomsEvent> {

    final protected DomsId domsId;
    final protected DomsEventStorage<Item> domsEventStorage;
    final protected ObjectProfile objectProfile;
    final protected DomsEventAdderCollection domsEvents;
    final protected DomsItemDatastreams domsItemDatastreams;

    public DomsItem(DomsId domsId, DomsEventStorage<Item> domsEventStorage, ObjectProfile objectProfile) {
        this.domsId = domsId;

        this.domsEventStorage = domsEventStorage;
        this.objectProfile = objectProfile;

        // FIXME:
        this.domsEvents = new DomsEventAdderCollection(domsId, domsEventStorage);
        this.domsItemDatastreams = new DomsItemDatastreams(domsId, domsEventStorage, key -> "Added at " + new Date());
    }

    @Override
    public String toString() {
        return "T"; // + item.getEventList().size(); // FIXME:  shortcut around getting unique eventtype.
    }

    @Override
    public Map<String, String> datastreams() {
        return domsItemDatastreams;
    }

    @Override
    public Collection<DomsEvent> events() {
        return domsEvents;
    }

}
