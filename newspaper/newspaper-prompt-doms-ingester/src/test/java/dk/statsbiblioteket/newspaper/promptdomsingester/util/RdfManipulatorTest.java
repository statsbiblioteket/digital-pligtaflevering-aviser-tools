package dk.statsbiblioteket.newspaper.promptdomsingester.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by csr on 6/13/14.
 */
public class RdfManipulatorTest {
    private static Logger logger = LoggerFactory.getLogger(RdfManipulator.class);

    public static final String rdf1 = "<rdf:RDF xmlns:doms=\"http://doms.statsbiblioteket.dk/relations/default/0/1/#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
            "  <rdf:Description rdf:about=\"info:fedora/uuid:65e7ece1-1b90-4f12-9f8f-c8e77b354f66\">\n" +
            "    <hasModel xmlns=\"info:fedora/fedora-system:def/model#\" rdf:resource=\"info:fedora/doms:ContentModel_RoundTrip\"></hasModel>\n" +
            "    <hasModel xmlns=\"info:fedora/fedora-system:def/model#\" rdf:resource=\"info:fedora/doms:ContentModel_DOMS\"></hasModel>\n" +
            "    <doms:isPartOfCollection rdf:resource=\"info:fedora/doms:Newspaper_Collection\"></doms:isPartOfCollection>\n" +
            "    <hasPart xmlns=\"info:fedora/fedora-system:def/relations-external#\" rdf:resource=\"info:fedora/uuid:05d840bf-8bb6-48e5-b214-2ab39f6259f8\"></hasPart>\n" +
            "    <hasPart xmlns=\"info:fedora/fedora-system:def/relations-external#\" rdf:resource=\"info:fedora/uuid:c625596a-9bbc-4331-b55c-beb55a3b80fe\"></hasPart>\n" +
            "  </rdf:Description>\n" +
            "</rdf:RDF>";

    /**
     * Test that we can use the class to convert back and forward to a String representation.
     */
    @Test
    public void testIO() {
        RdfManipulator rdfManipulator = new RdfManipulator(rdf1);
        String output = rdfManipulator.toString();
        RdfManipulator rdfManipulator1 = new RdfManipulator(output);
        logger.debug("\n" + output);
    }

    @Test
    public void testAddContentModel() {
        RdfManipulator rdfManipulator = new RdfManipulator(rdf1);
        rdfManipulator.addContentModel("doms:ContentModel_testmodel");
        assertTrue(rdfManipulator.toString().contains("testmodel"), rdfManipulator.toString());
        logger.debug(rdfManipulator.toString());
    }

    @Test
    public void testAddExternalRelation() {
        RdfManipulator rdfManipulator = new RdfManipulator(rdf1);
        rdfManipulator.addExternalRelation("hasFoobar", "uuid:barfoo");
        String rdf = rdfManipulator.toString();
        assertTrue(rdf.contains("info:fedora/uuid:barfoo"), rdf);
        assertTrue(rdf.contains("hasFoobar"), rdf);
        logger.debug(rdf);
    }
    
    @Test
    public void testAddExistingExternalRelation() {
        final String testUUID = "uuid:c625596a-9bbc-4331-b55c-beb55a3b80fe";
        RdfManipulator rdfManipulator = new RdfManipulator(rdf1);
        rdfManipulator.addExternalRelation("hasPart", testUUID);
        String rdf = rdfManipulator.toString();
        assertFalse(rdf.replaceFirst(testUUID, "").contains(testUUID), "Relation appears two times, it should not.");
    }
}
