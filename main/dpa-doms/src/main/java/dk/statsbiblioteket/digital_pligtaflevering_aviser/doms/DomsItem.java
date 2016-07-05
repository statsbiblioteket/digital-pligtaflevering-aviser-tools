package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

import java.util.Date;

/**
 *
 */
public class DomsItem<I extends Item> implements DomsEventAdder {

    private final I item;
    private final DomsEventStorage<I> domsEventStorage;

    public DomsItem(I item, DomsEventStorage<I> domsEventStorage) {
        this.item = item;
        this.domsEventStorage = domsEventStorage;
    }

    @Override
    public boolean add(DomsEvent event) {
        try {
            Date d = domsEventStorage.appendEventToItem(
                    item,
                    event.getAgent(),
                    new Date(),
                    event.getDetails(),
                    event.getEventType(),
                    event.getOutcome());
            System.out.println(d);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Item getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "T" + item.getEventList().size(); // FIXME:  shortcut around getting unique eventtype.
    }
}
