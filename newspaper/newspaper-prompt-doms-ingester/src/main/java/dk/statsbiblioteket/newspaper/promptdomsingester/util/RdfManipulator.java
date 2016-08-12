package dk.statsbiblioteket.newspaper.promptdomsingester.util;

import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;

/**
 * Simple class for manipulating xml representation of rdf datastream. All the methods for adding
 * elements to the rdf implicitly include deduplication. Ie the relation is only added if it is not already present.
 *
 */
public class RdfManipulator {
    private static final String DOMS_PREDICATE_NAMESPACE = "http://doms.statsbiblioteket.dk/relations/default/0/1/#";
    Document document;
    Node rdfDescriptionNode;

    public static final String MODEL_TEMPLATE =  "<hasModel xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
            + "xmlns=\"info:fedora/fedora-system:def/model#\" "
            + "rdf:resource=\"info:fedora/PID\"/>";

    public static final String EXTERNAL_RELATION_TEMPLATE = "<NAME xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
            + "xmlns=\"http://doms.statsbiblioteket.dk/relations/default/0/1/#\" "
            + "rdf:resource=\"info:fedora/PID\"/>";

    public static final XPathSelector X_PATH_SELECTOR = DOM.createXPathSelector(
            "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "doms", "http://doms.statsbiblioteket.dk/relations/default/0/1/#",
            "relsExt", "info:fedora/fedora-system:def/relations-external#"
    );

    /**
     * CTOR for the class
     * @param xml String representation of the rdf datastream,
     */
    public RdfManipulator(String xml) {
        document = DOM.stringToDOM(xml, true);
        rdfDescriptionNode = X_PATH_SELECTOR.selectNode(document, "//rdf:Description");
    }

    /**
     * Returns the rdf datastream xml as a String.
     * @return the manipulated datastream.
     */
    @Override
    public String toString() {
        try {
            return DOM.domToString(document);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Incorporates the given fragment of rdf/xml as a rdf:description node of the given document
     * @param fragment The rdf/xml fragment to be added
     */
    public void addFragmentToDescription(Fragment fragment) {
        if(!containsFragment(fragment)) {
            Document fragmentNode = DOM.stringToDOM(fragment.toString(), true);
            Node importedNode = document.importNode(fragmentNode.getDocumentElement(), true);
            rdfDescriptionNode.appendChild(importedNode);
        }
    }


    /**
     * Adds a content model.
     * @param modelPid the full pid of the model, including prefix e.g. "doms:ContentModel_DOMS".
     */
    public RdfManipulator addContentModel(String modelPid) {
        return addRelation("info:fedora/fedora-system:def/model#", "hasModel", modelPid);
    }

    /**
     * Adds a new named external relation from this object to another object, e.g. to add
     * <hasPart xmlns="info:fedora/fedora-system:def/relations-external#" rdf:resource="info:fedora/uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"/>
     * just call this method with the parameters ("hasPart", "uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8")
     * @param predicateName the short name of the relation, e.g. "hasPart"
     * @param objectPid the full doms pid of the object of the relation, e.g. "uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"
     */
    public RdfManipulator addExternalRelation(String predicateName, String objectPid) {
        return addRelation("info:fedora/fedora-system:def/relations-external#", predicateName, objectPid);
    }
    
    /**
     * Adds a new named doms-relation from this object to another object, e.g.
     * <hasPart xmlns="http://doms.statsbiblioteket.dk/relations/default/0/1/#" rdf:resource="info:fedora/uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"/>
     * @param predicateName the short name of the relation, e.g. "hasPart"
     * @param objectPid the full doms pid of the object of the relation, e.g. "uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"
     */
    public RdfManipulator addDomsRelation(String predicateName, String objectPid) {
        return addRelation(DOMS_PREDICATE_NAMESPACE, predicateName, objectPid);
        
    }

    /**
     * Remove all content model relations.
     */
    public void clearOldContentModels() {
        XPathSelector selector = DOM.createXPathSelector("our", "info:fedora/fedora-system:def/model#",
                "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String xpath = "//our:" + "hasModel";
        NodeList nodes = selector.selectNodeList(rdfDescriptionNode, xpath);
        if(nodes != null) {
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                node.getParentNode().removeChild(node);
            }
        }
    }

    /**
     * Remove relations with predicates in the DOMS namespace with the given names.
     *
     * @param predicates Local part of the DOMS predicates
     */
    public void clearDomsRelations(String... predicates) {
        XPathSelector selector = DOM.createXPathSelector("our", DOMS_PREDICATE_NAMESPACE,
                "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        for (String predicate : predicates) {
            String xpath = "//our:" + predicate;
            NodeList nodes = selector.selectNodeList(rdfDescriptionNode, xpath);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    node.getParentNode().removeChild(node);
                }
            }
        }
    }

    /**
     * Class representing an xml-fragment (a single relation) from an rdf-xml document.
     */
    static public class Fragment {
        private String predicateNS;
        private String predicateName;
        private String object;

        /**
         * Constructor for this class.
         * @param predicateNS Namespace of predicate, for example "http://doms.statsbiblioteket.dk/relations/default/0/1/#"
         * @param predicateName Name of the predicate, for example "hasPart"
         * @param object uuid (doms pid) of the object of the relation, e.g. "uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8"
         */
        public Fragment(String predicateNS, String predicateName, String object) {
            this.predicateNS = predicateNS;
            this.predicateName = predicateName;
            this.object = object;
        }
        
        public String getPredicateNS() {
            return predicateNS;
        }

        public String getPredicateName() {
            return predicateName;
        }

        public String getObject() {
            return object;
        }

        public String toString() {
            return "<" + predicateName + " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
                    + "xmlns=\"" + predicateNS + "\" rdf:resource=\"info:fedora/" + object + "\"/>";
        }
    }

    private boolean containsFragment(Fragment fragment) {
        XPathSelector selector = DOM.createXPathSelector("our", fragment.getPredicateNS(),
                "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        String xpath = "//our:" + fragment.getPredicateName()
                + "[@rdf:resource='info:fedora/" + fragment.getObject() + "']";
        Node node = selector.selectNode(rdfDescriptionNode, xpath);
        if(node == null) {
            return false;
        } else {
            return true;
        }
    }

    private RdfManipulator addRelation(String predicateNS, String predicateName, String objectPid) {
           Fragment frag = new Fragment(predicateNS, predicateName, objectPid);
           addFragmentToDescription(frag);
           return this;
       }


}
