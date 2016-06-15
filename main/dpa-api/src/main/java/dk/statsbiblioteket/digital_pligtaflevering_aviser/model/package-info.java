/**
 * <p>Basic interfaces for DPA.
 * </p>
 * <p>The core model of DPA is:
 * <dl>
 * <dt><b>Repository</b>:</dt>
 * <dd>An collection of Items which can be selected with a Query.
 * new and updated Items can be put back into the Repository.
 * </dd>
 * <dt><b>Item:</b></dt>
 * <dd>The working term for something that a Query can retrieve from a Repository.  The only
 * core functionality is the ability to add an Event.
 * </dd>
 * <dt><b>Event:</b></dt>
 * <dd>Something that has happened for a given Item. Actual details are implementation dependent.</dd>
 * <p>
 * </dl></p>
 */
package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;