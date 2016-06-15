package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

/**
 * An EventAdder is implemented for Items retrieved from a Repository to add Events to the Item.
 */
public interface EventAdder<E extends Event> {
    /**
     * <p>
     * This allows a Collection<E> @see{@link java.util.Collection<E>#add(Event)} sub class to implement this method
     * directly.
     * </p>
     *
     * @param event
     * @return
     */
    boolean add(E event);
}
