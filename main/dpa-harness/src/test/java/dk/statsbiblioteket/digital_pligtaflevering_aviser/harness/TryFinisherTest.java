package dk.statsbiblioteket.digital_pligtaflevering_aviser.harness;

import javaslang.control.Try;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

/**
 * Check that the TryFinisher actually invokes the passed-in string converter to generate a report for the non-failing
 * Try-entries, and that the invoked StackTrace renderer method is actually invoked for each failing Try-entry with a
 * blank line between each and that the final report consist of these two concatenated.
 */
public class TryFinisherTest {

    @Test
    public void test1() {

        final TryFinisher<String> tf = new TryFinisher<>(s -> "> " + s.count(), Throwable::getMessage);

        assertEquals("> 0", tf.apply(emptyList()));

        assertEquals("> 1", tf.apply(asList(Try.of(() -> "!"))));

        assertEquals("> 2", tf.apply(asList(Try.of(() -> "!"), Try.of(() -> " not ok"))));


        assertEquals("> 1\n\nA", tf.apply(asList(Try.of(() -> "!"), Try.of(() -> {
            throw new RuntimeException("A");
        }))));

        assertEquals("> 2\n\nA", tf.apply(asList(Try.of(() -> "!"), Try.of(() -> {
            throw new RuntimeException("A");
        }), Try.of(() -> "!"))));

        assertEquals("> 1\n\nA\n\nB\n\nC", tf.apply(
                asList(Try.of(() -> "!"),
                        Try.of(() -> {
                            throw new RuntimeException("A");
                        }), Try.of(() -> {
                            throw new RuntimeException("B");
                        }), Try.of(() -> {
                            throw new RuntimeException("C");
                        }))
        ));
    }
}
