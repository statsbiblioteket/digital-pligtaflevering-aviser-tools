package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FedoraIngesterTestUnit extends AbstractFedoraIngesterTest {

    EnhancedFedoraStub fedora = null;

    @Override
    protected EnhancedFedora getEnhancedFedora() throws MalformedURLException, JAXBException, PIDGeneratorException {
        List<String> datastreamNames = new ArrayList<>();
        datastreamNames.add("mods");
        datastreamNames.add("film");
        datastreamNames.add("edition");
        datastreamNames.add("alto");
        datastreamNames.add("mix");
        this.fedora = new EnhancedFedoraStub(datastreamNames);
        return fedora;
    }

    @Test
    public void testIngest() throws Exception {
        super.testIngest(new SimpleFedoraIngester(getEnhancedFedora(), new String[]{"Newspapers"}));
        System.out.println("Created " + fedora.objectsCreated + " objects.");
        System.out.println("Modified " + fedora.datastreamsModified + " datastreams.");
        System.out.println(fedora.toString());
    }
}
