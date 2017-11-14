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

/** @noinspection Convert2MethodRef, ArraysAsListWithZeroOrOneArgument */
public class IdValueTest {
    @Test
    public void basicFunctionality() {
        String s1 = "s1";
        String s2 = "s2";
        IdValue<String, String> iv = new IdValue<>(s1, s2);
        Assert.assertEquals(s1, iv.id());
        Assert.assertEquals(s2, iv.value());
    }

    @Test
    public void map0() {
        Map<String, String> m = Stream.of("1", "2", "3")
                .map(IdValue::create)
                .collect(toMap(IdValue::id, IdValue::value));

        Map<String, String> expected = new TreeMap<>();
        expected.put("1", "1");
        expected.put("2", "2");
        expected.put("3", "3");

        assertThat(m, is(expected));
    }

    @Test
    public void map1() {
        Map<String, String> m = Stream.of("1", "2", "3")
                .map(IdValue::create)
                .map(c -> c.map(v -> Integer.valueOf(v) * 2)) // string -> int
                .map(c -> c.map((id, v) -> id + "!" + v)) // back to string
                .collect(toMap(IdValue::id, IdValue::value));

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
                .map(id -> new IdValue<>(id, id + "!"))
                .map(c -> c.map(v -> ">" + v))
                .map(c -> c.map((id, v) -> v.substring(id - 1)))
                .collect(toMap(c -> c.id(), c -> c.value()));

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
                .map(id -> new IdValue<>(id, Math.PI * id))
                .map(c -> c.map(v -> Double.toString(v).substring(0, 4)))
                .collect(toMap(IdValue::id, IdValue::value));

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
                .map(IdValue::create)
                .filter(c -> c.filter(v -> v < 2))
                .collect(toMap(IdValue::id, IdValue::value));

        Map<Integer, Integer> expected = new TreeMap<>();
        expected.put(1, 1);

        assertThat(m, is(expected));
    }

    @Test
    public void flatMap1() {
        Map<Integer, Integer> m = Stream.of(1, 2, 3)
                .map(IdValue::create)
                .flatMap(c -> c.flatMap(v -> v % 2 == 1 ? Stream.of(v * 2) : Stream.of()))
                .collect(toMap(c -> c.id(), c -> c.value()));

        Map<Integer, Integer> expected = new TreeMap<>();
        expected.put(1, 2);
        expected.put(3, 6);

        assertThat(m, is(expected));
    }

    @Test
    public void flatMap2() {
        Map<Integer, List<Integer>> m = Stream.of(1, 2, 3)
                .map(IdValue::create)
                .flatMap(c -> c.flatMap((id, v) -> id % 2 == 1 ? Stream.of(v * 2) : Stream.of(v, v * 2, v * 3)))
                .collect(groupingBy(c -> c.id(), mapping(c -> c.value(), toList()))); // multi-valued map

        Map<Integer, List<Integer>> expected = new TreeMap<>();
        expected.put(1, Arrays.asList(2));
        expected.put(2, Arrays.asList(2, 4, 6));
        expected.put(3, Arrays.asList(6));

        assertThat(m, is(expected));
    }
}