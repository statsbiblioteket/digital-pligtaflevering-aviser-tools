package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 *
 */
public class VeraPDFWebServices {

    /**
     * @param baseURL e.g. "http://localhost:8080/api"
     */
    public VeraPDFWebServices(String baseURL) {
        this.baseURL = Objects.requireNonNull(baseURL, "baseURL == null");
    }

    final String baseURL;

//    public String getInfo() {
//        // https://jersey.java.net/documentation/latest/user-guide.html#d0e4152
//        Client client = ClientBuilder.newClient();
//        WebTarget target = client.target(baseURL).path("info");
//
//        Response response = target.request(MediaType.APPLICATION_JSON).get();
//        return response.readEntity(String.class);
//    }

    public String getIds() {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);

        WebResource resource = client.resource(UriBuilder.fromUri(baseURL + "/profiles/ids").build());

        ClientResponse response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);

        return response.getEntity(String.class);
    }

    public static void main(String[] args) throws FileNotFoundException {
        File pdfFile = new File("/home/tra/git/digital-pligtaflevering-aviser-tools/main/dpa-verapdf/src/test/resources/veraPDF test suite 6-8-2-2-t01-fail-a.pdf");
        VeraPDFWebServices veraPDFWebServices = new VeraPDFWebServices("http://localhost:8080/api");
        System.out.println(veraPDFWebServices.getIds());
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  pdfFile.getName(), "1b"));
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  pdfFile.getName(), "3b"));
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  pdfFile.getName(), "1a"));
    }

    public String validate(InputStream inputStream, String fileName, String profileId) {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);

        WebResource resource = client.resource(UriBuilder.fromUri(baseURL + "/validate/" + profileId).build());

        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.bodyPart(new StreamDataBodyPart("file", inputStream, fileName));
        ClientResponse response = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class, multiPart);

        return response.getEntity(String.class);
    }

}
