package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 */
public class ContextValue<C, V> {
    public C getContext() {
        return context;
    }

    public V getValue() {
        return value;
    }

    final C context;
    final V value;

    protected ContextValue(C context, V value) {
        this.context = context;
        this.value = value;
    }

    /** Return new ContextValue object with the given value and the same context
     *
     * @param value value for new object.
     * @return
     */
    public <U> ContextValue<C, U> of(U value){
        return new ContextValue(context, value);
    }

    /**
     * Apply a given function to the value and return a new ContextValue object with the result (and same context).
     * @param f function to apply to the current value to get the new value.
     * @return
     */
    public <U> ContextValue<C, U> of(Function<V, U> f) {
        return of(f.apply(value));
    }

    /** Apply a given two-argument function to the context <b>and</b> the current value, and return a new object with
     * the result (and the same context).
     * @param f function to apply to context and value to get new value.
     * @return
     */
    public <U> ContextValue<C, U> of(BiFunction<C, V, U> f) {
        return of(f.apply(context, value));
    }
}
