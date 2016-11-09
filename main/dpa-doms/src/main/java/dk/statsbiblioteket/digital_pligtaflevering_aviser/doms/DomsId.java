package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

/**
 *
 */
public class DomsId implements Id {

    private String id;

    public DomsId(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return "DomsId{" +
                "id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomsId domsId = (DomsId) o;

        return id.equals(domsId.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
