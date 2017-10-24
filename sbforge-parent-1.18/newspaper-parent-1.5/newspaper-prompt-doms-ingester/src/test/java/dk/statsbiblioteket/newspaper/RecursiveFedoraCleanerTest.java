package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.newspaper.promptdomsingester.FedoraIngesterTestIT;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 08/10/13
 * Time: 08:42
 * To change this template use File | Settings | File Templates.
 */
public class RecursiveFedoraCleanerTest {

    @Test(groups = "externalTest")
    public void testCleanFedora() throws
                                  MalformedURLException,
                                  JAXBException,
                                  PIDGeneratorException,
                                  BackendInvalidCredsException,
                                  BackendMethodFailedException,
                                  BackendInvalidResourceException {
        EnhancedFedora fedora = (new FedoraIngesterTestIT()).getEnhancedFedora();
        String label = TestConstants.TEST_BATCH_PATH;
        RecursiveFedoraCleaner.cleanFedora(fedora, label, true);
    }


    public void testCleanFedoraWildcard() throws
                                          MalformedURLException,
                                          JAXBException,
                                          PIDGeneratorException,
                                          BackendInvalidCredsException,
                                          BackendMethodFailedException,
                                          BackendInvalidResourceException {
        EnhancedFedora fedora = (new FedoraIngesterTestIT()).getEnhancedFedora();
        String foundPid = new FedoraIngesterTestIT().getEnhancedFedora().findObjectFromDCIdentifier("path:*").get(0);
        while (foundPid != null) {
            System.out.println("Deleting object " + foundPid);
            RecursiveFedoraCleaner.deleteSingleObject(fedora, foundPid);
            foundPid = new FedoraIngesterTestIT().getEnhancedFedora().findObjectFromDCIdentifier("path:*").get(0);
        }
    }

}
