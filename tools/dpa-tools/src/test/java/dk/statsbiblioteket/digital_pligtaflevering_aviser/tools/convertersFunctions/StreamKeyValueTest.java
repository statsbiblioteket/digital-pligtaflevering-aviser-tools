package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class StreamKeyValueTest {
    @Test
    public void simple() throws Exception {
        String s1 = "s1";
        String s2 = "s2";
        StreamIdValue cv = new StreamIdValue(s1, s2);
        Assert.assertEquals(s1, cv.id());
        Assert.assertEquals(s2, cv.value());
    }

    @Test
    public void of1() throws Exception {
        Map m = Stream.of("1", "2", "3")
                .map(StreamIdValue::new)
                .map(cv -> cv.of((c, v) -> c + "!"))
                .collect(Collectors.toMap(cv -> cv.id(), cv -> cv.value()));

        Map<String, String> expected = new TreeMap<>();
        expected.put("1", "1!");
        expected.put("2", "2!");
        expected.put("3", "3!");

        assertThat(m, is(expected));
    }

    @Test
    public void of2() throws Exception {
        Map<Integer, String> m = Stream.of(1, 2, 3)
                .map(id -> new StreamIdValue<>(id, id + "!"))
                .map(c -> c.of(v -> ">" + v))
                .map(c -> c.of((id, v) -> v.substring(id - 1)))
                .collect(Collectors.toMap(cv -> cv.id(), cv -> cv.value()));

        Map<Integer, String> expected = new TreeMap<>();
        expected.put(1, ">1!");
        expected.put(2, "2!");
        expected.put(3, "!");

        assertThat(m, is(expected));
    }

    @Test
    public void of3() throws Exception {
        Map<Integer, String> m = Stream.of(1, 2, 3)
                .map(id -> new StreamIdValue<>(id, Math.PI * id))
                .map(c -> c.of(v -> Double.toString(v).substring(0,4)))
                .collect(Collectors.toMap(cv -> cv.id(), cv -> cv.value()));

        Map<Integer, String> expected = new TreeMap<>();
        expected.put(1, "3.14");
        expected.put(2, "6.28");
        expected.put(3, "9.42");

        assertThat(m, is(expected));
    }

}