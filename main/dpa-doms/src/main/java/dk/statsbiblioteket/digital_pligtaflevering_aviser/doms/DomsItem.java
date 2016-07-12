package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public class DomsItem<I extends Item> implements RepositoryItem<DomsEvent> {

    private final I item;
    private final DomsEventStorage<I> domsEventStorage;
    final private DomsEventAdderCollection<I> domsEvents;
    private final DomsItemDatastreams<I> domsItemDatastreams;

    public DomsItem(I item, DomsEventStorage<I> domsEventStorage) {
        this.item = item;
        this.domsEventStorage = domsEventStorage;
        this.domsEvents = new DomsEventAdderCollection<>(item, domsEventStorage);
        this.domsItemDatastreams = new DomsItemDatastreams<I>(item, domsEventStorage, key -> "Added at " + new Date());
    }

    public Item getOriginalItem() {
        return item;
    }

    @Override
    public String toString() {
        return "T" + item.getEventList().size(); // FIXME:  shortcut around getting unique eventtype.
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
