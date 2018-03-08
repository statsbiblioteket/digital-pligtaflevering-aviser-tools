package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.kb.streams.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 */
public class DomsIdTuple<V> extends StreamTuple<DomsItem, V> {
    public DomsIdTuple(DomsItem domsItem, V value) {
        super(domsItem, value);
    }

    public static DomsIdTuple<DomsItem> create(DomsItem id) {
        return new DomsIdTuple<>(id, id);
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
