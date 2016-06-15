package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;

public interface EventAdder<E extends Event> {
    /**
     * This allows a Collection<E> @see{@link java.util.Collection<E>#add(Event)} sub class to implement this method
     * directly.
     *
     * @param event
     * @return
     */
    boolean add(E event);
}
