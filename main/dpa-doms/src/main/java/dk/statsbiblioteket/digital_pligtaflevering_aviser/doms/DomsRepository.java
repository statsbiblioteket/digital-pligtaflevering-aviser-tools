package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Repository;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;

import java.util.stream.Stream;

/**
 *
 */
public abstract class DomsRepository<I extends Item> implements Repository<DomsEventAdder, DomsEvent, Stream<I>> {
    private DomsEventStorage<I> domsEventStorage;
    private SBOIEventIndex index;

    public DomsRepository(DomsEventStorage<I> domsEventStorage, SBOIEventIndex index) {
        this.domsEventStorage = domsEventStorage;
        this.index = index;
    }

    @Override
    public DomsEvent put(DomsEventAdder item, DomsEvent addValue) {
        return null;
    }


}
