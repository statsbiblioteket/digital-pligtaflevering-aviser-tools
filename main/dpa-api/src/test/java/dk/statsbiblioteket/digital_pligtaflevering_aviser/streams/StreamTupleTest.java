package dk.statsbiblioteket.digital_pligtaflevering_aviser.streams;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @noinspection Convert2MethodRef, ArraysAsListWithZeroOrOneArgument
 */
public class StreamTupleTest {
    @Test
    public void areLeftAndRightAsExpected() {
        String s1 = "s1";
        String s2 = "s2";
        StreamTuple<String, String> streamTuple = new StreamTuple<>(s1, s2);
        Assert.assertEquals(s1, streamTuple.left());
        Assert.assertEquals(s2, streamTuple.right());
    }

    @Test
    public void map0() {
        Map<String, String> m = Stream.of("1", "2", "3")
                .map(StreamTuple::create)
                .collect(toMap(t -> t.left(), t -> t.right()));

        Map<String, String> expected = new TreeMap<>();
        expected.put("1", "1");
        expected.put("2", "2");
        expected.put("3", "3");

        assertThat(m, is(expected));
    }

    @Test
    public void map1() {
        Map<String, String> m = Stream.of("1", "2", "3")
                .map(StreamTuple::create)
                .map(t -> t.map(v -> Integer.valueOf(v) * 2)) // string -> int
                .map(t -> t.map((id, v) -> id + "!" + v)) // back to string
                .collect(toMap(t -> t.left(), t -> t.right()));

        Map<String, String> expected = new TreeMap<>();
        expected.put("1", "1!2");
        expected.put("2", "2!4");
        expected.put("3", "3!6");

        assertThat(m, is(expected));
    }

    /**
     * @noinspection Convert2MethodRef
     */
    @Test
    public void map2() {
        // Do simple string manipulations using id.
        Map<Integer, String> m = Stream.of(1, 2, 3)
                .map(id -> new StreamTuple<>(id, id + "!"))
                .map(t -> t.map(r -> ">" + r))
                .map(t -> t.map((l, r) -> r.substring(l - 1)))
                .collect(toMap(t -> t.left(), t -> t.right()));

        Map<Integer, String> expected = new TreeMap<>();
        expected.put(1, ">1!");
        expected.put(2, "2!");
        expected.put(3, "!");

        assertThat(m, is(expected));
    }

    @Test
    public void of3() throws Exception {
        // Change value type several times.
        Map<Integer, String> m = Stream.of(1, 2, 3)
                .map(id -> new StreamTuple<>(id, Math.PI * id))
                .map(t -> t.map(r -> Double.toString(r).substring(0, 4)))
                .collect(toMap(t -> t.left(), t -> t.right()));

        Map<Integer, String> expected = new TreeMap<>();
        expected.put(1, "3.14");
        expected.put(2, "6.28");
        expected.put(3, "9.42");

        assertThat(m, is(expected));
    }

    @Test
    public void filter1() throws Exception {
        // Change value type several times.

        Map<Integer, Integer> m = Stream.of(1, 2, 3)
                .map(StreamTuple::create)
                .filter(t -> t.filter(r -> r < 2))
                .collect(toMap(t -> t.left(), t -> t.right()));

        Map<Integer, Integer> expected = new TreeMap<>();
        expected.put(1, 1);

        assertThat(m, is(expected));
    }

    @Test
    public void flatMap1() {
        Map<Integer, Integer> m = Stream.of(1, 2, 3)
                .map(StreamTuple::create)
                // get odd ones and multiply them by two
                .flatMap(t -> t.flatMap(r -> r % 2 == 1 ? Stream.of(r * 2) : Stream.of()))
                .collect(toMap(t -> t.left(), t -> t.right()));

        Map<Integer, Integer> expected = new TreeMap<>();
        expected.put(1, 2);
        expected.put(3, 6);

        assertThat(m, is(expected));
    }

    @Test
    public void flatMap2() {
        Map<Integer, List<Integer>> m = Stream.of(1, 2, 3)
                .map(StreamTuple::create)
                .flatMap(t -> t.flatMap((l, r) -> l % 2 == 1 ? Stream.of(r * 2) : Stream.of(r, r * 2, r * 3)))
                .collect(groupingBy(t -> t.left(), mapping(t -> t.right(), toList()))); // multi-valued map

        Map<Integer, List<Integer>> expected = new TreeMap<>();
        expected.put(1, Arrays.asList(2));
        expected.put(2, Arrays.asList(2, 4, 6));
        expected.put(3, Arrays.asList(6));

        assertThat(m, is(expected));
    }
}
