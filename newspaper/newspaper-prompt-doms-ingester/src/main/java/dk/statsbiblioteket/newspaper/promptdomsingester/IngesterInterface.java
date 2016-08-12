package dk.statsbiblioteket.newspaper.promptdomsingester;


import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

/**
 * Interface defining the functionality for an ingester.
 */
public interface IngesterInterface {

    /**
     * Given a iterator, this method ingests the contents of the root directory to DOMS with the
     * name of the root directory as the label of the root object in DOMS.
     *
     * @param iterator
     *
     * @return the DOMS pid of the root object.
     */
    String ingest(TreeIterator iterator);

}
