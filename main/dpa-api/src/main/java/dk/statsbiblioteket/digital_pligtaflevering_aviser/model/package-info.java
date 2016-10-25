/**
 * <p>Basic interfaces for DPA.
 * </p>
 * <p>The model model of DPA is:
 * <dl>
 * <dt><b>Repository</b>:</dt>
 * <dd>An collection of Items which can be selected with a RepositoryQuery.
 * new and updated Items can be put back into the Repository.
 * </dd>
 * <dt><b>RepositoryItem:</b></dt>
 * <dd>The working term for something that a RepositoryQuery can retrieve from a Repository.  An item have zero or more
 * named datastreams attached, and zero or more events attached (FIXME:  Define order).
 * </dd>
 * <dt><b>Datastream:</b></dt>
 * <dd>A datastream is a DOMS concept similar to a named file but without a notion of a directory tree structure.
 * A datastream always belong to exactly one item.</dd>
 * <dt><b>Event:</b></dt>
 * <dd>Something that has happened for a given RepositoryItem. Actual details are implementation dependent.</dd>
 * <p>
 * </dl></p>
 */
package dk.statsbiblioteket.digital_pligtaflevering_aviser.model;
