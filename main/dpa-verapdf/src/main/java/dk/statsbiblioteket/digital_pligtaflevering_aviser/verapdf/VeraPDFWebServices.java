package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    public String getInfo() {
        // https://jersey.java.net/documentation/latest/user-guide.html#d0e4152
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(baseURL).path("info");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        return response.readEntity(String.class);
    }

    public String getIds() {
        // https://jersey.java.net/documentation/latest/user-guide.html#d0e4152
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(baseURL).path("profiles").path("ids");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        return response.readEntity(String.class);
    }

    public static void main(String[] args) {
        InputStream pdfInputStream = VeraPDFWebServices.class.getResourceAsStream("veraPDF test suite 6-8-2-2-t01-fail-a.pdf");
        VeraPDFWebServices veraPDFWebServices = new VeraPDFWebServices("http://localhost:8080/api");
        System.out.println(veraPDFWebServices.getIds());
        System.out.println(veraPDFWebServices.validate(pdfInputStream, "1b"));
    }

    private String validate(InputStream pdfInputStream, String profileId) {
        // https://jersey.java.net/documentation/latest/user-guide.html#d0e4152
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(baseURL).path("validate").path("{id}");
        target.resolveTemplate("id", profileId);

        Form form = new Form();
        //form.param("file", );


        Response response = target.request(MediaType.APPLICATION_JSON).get();
        return response.readEntity(String.class);
    }
}
