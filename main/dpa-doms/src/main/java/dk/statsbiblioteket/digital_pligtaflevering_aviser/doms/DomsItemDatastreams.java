package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

import java.util.HashMap;

/**
 *
 */
public class DomsItemDatastreams<I extends Item> extends HashMap<String, String> {

    private final I item;
    private final DomsEventStorage<I> domsEventStorage;

    public DomsItemDatastreams(I item, DomsEventStorage<I> domsEventStorage) {
        this.item = item;
        this.domsEventStorage = domsEventStorage;
    }

    @Override
    public String put(String key, String value) {
        // FIXME:  INvoke domseventstorage.
        System.out.println(key + " -> " + value);
        return super.put(key, value);
    }
}
