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
    /**
     * query for all items corresponding to the given query
     */
    V query(Q query);

    /**
     * Ask how many items a given query would return without actually returning the items.
     */
    long count(Q query);
}
