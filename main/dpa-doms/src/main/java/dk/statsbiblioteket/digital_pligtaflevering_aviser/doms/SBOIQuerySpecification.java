package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

/**
 *
 */
public class SBOIQuerySpecification implements QuerySpecification {
    /**
     *
     * @param q is the "q" string in the Summa search web page, i.e. the request to make.
     */
    public SBOIQuerySpecification(String q) {
        this.q = q;
    }

    public String getQ() {
        return q;
    }

    final String q;
}
