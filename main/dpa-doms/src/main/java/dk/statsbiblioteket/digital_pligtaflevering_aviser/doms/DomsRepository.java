package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Repository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.SBOIEventIndex;

import java.util.stream.Stream;

/**
 *
 */
public class DomsRepository<I extends Item> implements Repository<RepositoryItem<DomsEvent>, DomsEvent, Stream<I>> {
    private DomsEventStorage<I> domsEventStorage;
    private SBOIEventIndex index;

    public DomsRepository(DomsEventStorage<I> domsEventStorage, SBOIEventIndex index) {
        this.domsEventStorage = domsEventStorage;
        this.index = index;
    }

    @Override
    public Stream<RepositoryItem<DomsEvent>> query(Stream<I> query) {
        return null;
    }
}
