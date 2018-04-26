package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.Id;

import java.util.Objects;

/**
 * A DomsId is a typed string uniquely identifying an object in DOMS. It does
 * not know of anything else.
 */
public class DomsId implements Id {

    public static final  String DPA_WEBRESOURCE = "dpa.webresource";

    private String id;

    public DomsId(String id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return "DomsId{"
                + "id='" + id + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomsId domsId = (DomsId) o;

        return id != null ? id.equals(domsId.id) : domsId.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
