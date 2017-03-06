package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import javaslang.control.Try;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Test the behavior of Try in the use scenario we have.  Many invocations of the same with
 * different input, and we need failures to be programmatically delayed to the end of the
 * stream.
 */
public class TryInStreamsTest {

    final long zero = Collections.emptyMap().size(); // not folded to constant 0 at compile time

    @Test
    public void noExceptionIsSuccess() {
        assertThat(Try.of(() -> 1).isSuccess(), is(true));
    }

    @Test
    public void divisionByZeroExceptionIsNotSuccessAndNotThrown() {
        assertThat(Try.of(() -> 1 / zero).isSuccess(), is(false));
    }

    @Test
    public void basicExceptionsInStreams() {
        // basic skeleton for generating a list of results and compare each entry in the list.
        assertThat(Stream.of(0).map(l -> l).collect(toList()), hasItems(is(0)));

        // same with Try (split in two maps so the first return value is Try<...>.
        assertThat(Stream.of(0).map(l -> Try.of(() -> l)).map(t -> t.get()).collect(toList()), hasItems(is(0)));

        // now cause an exception and filter it out
        assertThat(Stream.of(0).map(l -> Try.of(() -> 1/l)).filter(Try::isSuccess).collect(toList()), hasItems());

        // result collector behavior
        Stream<Try<Integer>> s = Stream.of(-1, 0, 1).map(l -> Try.of(() -> 1 / l));
        Map<Boolean, List<Try<Integer>>> p = s.collect(Collectors.partitioningBy(Try::isSuccess));
        final List<Try<Integer>> failedTries = p.get(FALSE);
        // failedTries.forEach(t -> t.getCause().printStackTrace(System.out));
        assertThat(failedTries.size(), is(1));
        assertThat(Try.sequence(failedTries).isSuccess(), is(FALSE));
        List<Integer> s2 = p.get(TRUE).stream().map(Try::get).collect(toList());
        assertThat(s2, hasItems(is(-1), is(1)));




    }

//    @Test
//    public void exceptionsInStreams() {
//        assertThat(IntStream.of(0, 1).map(Try.of(i -> 1/i).isSuccess()).collect(Collectors.toList()), is(List.of(false, true)));
//    }

}
