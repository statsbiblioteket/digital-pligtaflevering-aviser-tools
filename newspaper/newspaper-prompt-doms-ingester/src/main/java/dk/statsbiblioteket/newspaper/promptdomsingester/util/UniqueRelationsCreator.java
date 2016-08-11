package dk.statsbiblioteket.newspaper.promptdomsingester.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.utils.FedoraUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encapsulates the necessary fedora calls to add rdf relations to a DOMS object.
 */
public class UniqueRelationsCreator {

    private EnhancedFedora fedora;

    /**
     * Constructor for this class.
     * @param fedora the fedora instance with which to communicate.
     *
     */
    public UniqueRelationsCreator(EnhancedFedora fedora) {
        this.fedora = fedora;
    }

    /**
     * Add the given rdf-relations. The relations are only added if they would not duplicate existing relations. This
     * method tries just once and throws an exception if it fails.
     * @param addRelationsRequest encapsulates the relations to be added.
     * @return the number of relations actually added.
     * @throws Exception if the method fails.
     */
    public int addRelationships(AddRelationsRequest addRelationsRequest) throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
        String pid = addRelationsRequest.getPid();
        Date lastModifiedDate = fedora.getObjectProfile(pid, null).getObjectLastModifiedDate();
        final String predicate = addRelationsRequest.getPredicate();
        List<FedoraRelation> existingRelations = fedora.getNamedRelations(
                pid,
                predicate,
                lastModifiedDate.getTime()
        );
        List<String> objects = clean(addRelationsRequest.getObjects());
        List<String> objectsToRemove = new ArrayList<>();
        for (FedoraRelation existingRelation: existingRelations) {
            for (String object: objects) {
                if (FedoraUtil.ensurePID(existingRelation.getObject()).equals(object)) {
                    objectsToRemove.add(object);
                }
            }
        }
        objects.removeAll(objectsToRemove);
        RdfManipulator rdfManipulator = new RdfManipulator(fedora.getXMLDatastreamContents(pid, "RELS-EXT"));
        String predicateNS = predicate.split("#")[0] + "#";
        String predicateName = predicate.split("#")[1];
        for (String object: objects) {
             rdfManipulator.addFragmentToDescription(new RdfManipulator.Fragment(predicateNS, predicateName, object));
        }
        if (!objects.isEmpty()) {
            fedora.modifyDatastreamByValue(pid, "RELS-EXT",
                    null,
                    null,
                    rdfManipulator.toString().getBytes(),
                    new ArrayList<String>(), "application/rdf+xml",
                    addRelationsRequest.getComment(),
                    lastModifiedDate.getTime());
        }
        return objects.size();
    }

    private List<String> clean(List<String> objects) {
        ArrayList<String> result = new ArrayList<>(objects.size());
        for (String object : objects) {
            result.add(FedoraUtil.ensurePID(object));
        }
        return result;
    }
}
