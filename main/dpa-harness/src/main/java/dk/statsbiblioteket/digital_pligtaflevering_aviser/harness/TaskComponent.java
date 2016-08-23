package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import dagger.Component;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.RepositoryItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Task;

import java.util.stream.Stream;


@Component
public interface TaskComponent<I extends RepositoryItem, V> {
    Stream<I> provideItemSteam();
    Task<I, V> provideTask();
    // Consumer<V> - what to do with the result?
}
