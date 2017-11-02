package dk.statsbiblioteket.digital_pligtaflevering_aviser.streams;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One of the major problems with using streams in Java is that the language does not support tuples (multiple return
 * values from a method) so you very frequently find that you need to pass _both_ the original value and the current
 * result to the next step instead of just the current result.  The current Word Of God is that you create a custom
 * class for each intermediate step, which is rather much a pain.   This is an experiment to see if a helper class that
 * knows the original value plus some suitable helper methods can replace these custom classes.
 *
 * @noinspection WeakerAccess
 */
public class IdValue<I, V> {

    protected final I id;
    protected final V value;

    /**
     *
     */
    protected IdValue(I id, V value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Suitable for method::references.  Value is set to id.
     */

    public static <I> IdValue<I, I> create(I id) {
        return new IdValue<>(id, id);
    }

    public I id() {
        return id;
    }

    public V value() {
        return value;
    }

    /**
     * Return new IdValue object with the given value (which may be a completely different type than the one hold by
     * this object) and the same context.
     *
     * @param value value for new object.
     * @return
     */
    public <U> IdValue<I, U> of(U value) {
        return new IdValue<>(id, value);
    }

    /**
     * Apply a given function to the value and return a new IdValue object with the result (and same context).
     *
     * @param f function to apply to the current value to get the new value.
     * @return
     */
    public <U> IdValue<I, U> map(Function<V, U> f) {
        return of(f.apply(value));
    }

    /**
     * Apply a given two-argument function to the context <b>and</b> the current value, and return a new object with the
     * result (and the same context).
     *
     * @param f function to apply to context and value to get new value.
     * @return
     */
    public <U> IdValue<I, U> map(BiFunction<I, V, U> f) {
        return of(f.apply(id, value));
    }

    /**
     *
     */
    public boolean filter(Predicate<V> predicate) {
        return predicate.test(value);
    }
}
