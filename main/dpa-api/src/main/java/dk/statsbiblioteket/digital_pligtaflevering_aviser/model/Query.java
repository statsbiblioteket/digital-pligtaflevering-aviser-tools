package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

public interface Query<Q, V> {
    // Should query be an attribute on the repository or a stand-alone thing?
    V query(Q query);
}
