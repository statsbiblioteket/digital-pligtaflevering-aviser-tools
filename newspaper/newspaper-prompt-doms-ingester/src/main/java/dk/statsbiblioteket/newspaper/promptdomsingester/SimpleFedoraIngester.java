package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A concrete implementation of AbstractFedoraIngester which should be capable of traversing and ingesting any
 * hierarchically structured collection of metadata and data files satisfying the assumptions described in
 * https://sbforge.org/display/NEWSPAPER/DOMS+object+model+creation+from+tree
 *
 * There is a utility factory method for the specific case of the Newspaper collection.
 */
public class SimpleFedoraIngester extends AbstractFedoraIngester {

    private final EnhancedFedora fedora;
    private final String[] collections;


    /**
     * Constructor for this method.
     *
     * @param fedora      the fedora instance in which to ingest.
     * @param collections the DOMS collections in which to ingest objects.
     */
    public SimpleFedoraIngester(EnhancedFedora fedora,

                                String[] collections) {
        this.fedora = fedora;
        this.collections = collections;
    }

    /**
     * A factory method to return an ingester tailored to the newspaper collection in which.
     *
     * @param fedora the fedora in which to ingest.
     *
     * @return the ingester.
     */
    @Deprecated
    public static SimpleFedoraIngester getNewspaperInstance(EnhancedFedora fedora) {
        return new SimpleFedoraIngester(fedora, new String[]{"info:Batch"});
    }

    /**
     * A factory method to return an ingester
     *
     * @param fedora the fedora in which to ingest.
     *
     * @return the ingester.
     */
    public static SimpleFedoraIngester getNewspaperInstance(EnhancedFedora fedora, String[] collections) {
        return new SimpleFedoraIngester(fedora, collections);
    }


    @Override
    public EnhancedFedora getEnhancedFedora() {
        return fedora;
    }


    @Override
    public List<String> getCollections() {
        return new ArrayList<>(Arrays.asList(collections));
    }

}
