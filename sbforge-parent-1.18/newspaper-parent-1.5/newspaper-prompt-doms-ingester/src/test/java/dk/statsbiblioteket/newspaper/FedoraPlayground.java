package dk.statsbiblioteket.newspaper;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.FedoraRest;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PidGeneratorImpl;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 03/10/13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
public class FedoraPlayground {

    //fedora.admin.username=fedoraAdmin
    //fedora.admin.password=fedoraAdminPass
    //fedora.server=http://achernar:7880/fedora/objects/
    //pidgenerator.location=http://achernar:7880/pidgenerator-service

    public static void main(String[] args) throws
                                           MalformedURLException,
                                           JAXBException,
                                           PIDGeneratorException,
                                           UnsupportedEncodingException {
        Credentials creds = new Credentials("fedoraAdmin", "fedoraAdminPass");
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(
                creds,
                "http://achernar:7880/fedora",
                "http://achernar:7880/pidgenerator-service",
                null);
        FedoraRestExtras fedora = new FedoraRestExtras(creds, "http://achernar:7880/fedora");
        PidGeneratorImpl pidGenerator = new PidGeneratorImpl("http://achernar:7880/pidgenerator-service");
        String pid = pidGenerator.generateNextAvailablePID("newspaper");
        System.out.println(pid);
        System.out.println(fedora.createObject(pid));
    }

    public static class FedoraRestExtras extends FedoraRest {

        Credentials creds;

        public FedoraRestExtras(Credentials creds, String location) throws MalformedURLException {
            super(creds, location);
            this.creds = creds;
        }

        public String createObject(String pid) throws UnsupportedEncodingException {
            WebResource resource = client.resource(location + "/objects");
            resource.addFilter(new HTTPBasicAuthFilter(creds.getUsername(), creds.getPassword()));
            return resource.path("/")
                           .path(URLEncoder.encode(pid, "UTF-8"))
                           .type(MediaType.TEXT_XML_TYPE)
                           .post(String.class);
        }

    }
}
