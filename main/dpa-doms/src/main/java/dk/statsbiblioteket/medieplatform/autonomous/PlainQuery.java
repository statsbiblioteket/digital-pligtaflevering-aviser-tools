package dk.statsbiblioteket.medieplatform.autonomous;

/**
 *
 */
public class PlainQuery<T extends Item> extends EventTrigger.Query<T> {

    final protected String q;

    public PlainQuery(String q) {
        this.q = q;
    }

    public String getQ() {
        return q;
    }
}
