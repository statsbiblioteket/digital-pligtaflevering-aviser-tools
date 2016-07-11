package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

/**
 * <p>
 * RepositoryQuery provides a method for retrieving objects for a given input.
 * </p><p>
 * It is still a draft class, and may be simplified to a Function&lt;Q,V&gt; (but which is
 * very cumbersomely named).</p>
 *
 * @param <Q> The query class.
 * @param <V> The result returned.
 */
public interface RepositoryQuery<Q, V> {
    // Should query be an attribute on the repository or a stand-alone thing?
    V query(Q query);
}
