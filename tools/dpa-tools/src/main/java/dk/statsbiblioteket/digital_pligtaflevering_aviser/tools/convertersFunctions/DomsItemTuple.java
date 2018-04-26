package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *  Helper class to keep generics a bit simpler using StreamTuple if we _know_ the left side is a DomsItem of some sort.  Also
 *  has a stacktrace renderer (which for some reason is not in Java 10 yet).
 */
public class DomsItemTuple<V> extends StreamTuple<DomsItem, V> {
    public DomsItemTuple(DomsItem domsItem, V value) {
        super(domsItem, value);
    }

    public static DomsItemTuple<DomsItem> create(DomsItem id) {
        return new DomsItemTuple<>(id, id);
    }

    public static String stacktraceFor(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String s = sw.toString();
        pw.close();
        return s;
    }
}
