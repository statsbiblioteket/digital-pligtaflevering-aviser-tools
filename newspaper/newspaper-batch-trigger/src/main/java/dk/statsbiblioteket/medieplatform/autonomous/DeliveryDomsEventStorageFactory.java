package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

/**
 * Factory implementation of a DomsEventStorageFactory which generates a DeliveryDomsEventStorage-object
 */
public class DeliveryDomsEventStorageFactory extends DomsEventStorageFactory<Delivery> {

    public static final String BATCH_TEMPLATE = "doms:Template_Batch";
    public static final String ROUND_TRIP_TEMPLATE = "doms:Template_RoundTrip";
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";

    protected String batchTemplate = BATCH_TEMPLATE;
    protected String roundTripTemplate = ROUND_TRIP_TEMPLATE;
    protected String hasPartRelation = HAS_PART;

    @SuppressWarnings("deprecation")  //Credentials
    public DeliveryDomsEventStorage createDeliveryDomsEventStorage() throws
            JAXBException,
            PIDGeneratorException,
            MalformedURLException {
        Credentials creds = new Credentials(username, password);
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(creds,
                fedoraLocation.replaceFirst("/(objects)?/?$", ""),
                pidGeneratorLocation,
                null,
                retries,
                delayBetweenRetries);

        return new DeliveryDomsEventStorage(fedora, premisIdentifierType, batchTemplate,roundTripTemplate,hasPartRelation,eventsDatastream, null);
    }
}
