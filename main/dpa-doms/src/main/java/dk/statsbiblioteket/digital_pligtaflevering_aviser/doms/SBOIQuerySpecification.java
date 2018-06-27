package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import java.util.Objects;

/**
 * SBOIQuerySpecification holds a plain query string for asking SBOI.  It is prepended with recordBase:.... so typically
 * must start with an " AND " - clause.
 */
public class SBOIQuerySpecification implements QuerySpecification {
    /**
     * @param q is the "q" string in the Summa search web page, i.e. the request to make.
     */
    public SBOIQuerySpecification(String q) {
        this.q = Objects.requireNonNull(q, "q");
    }

    public String getQ() {
        return q;
    }

    final String q;

    public static String quote(String s) {
        // FIXME:  Validate that quotes inside quotes must be escaped in SBOI requests
        return "\"" + s.replace("\"", "\\\"") + "\"";
    }
}
