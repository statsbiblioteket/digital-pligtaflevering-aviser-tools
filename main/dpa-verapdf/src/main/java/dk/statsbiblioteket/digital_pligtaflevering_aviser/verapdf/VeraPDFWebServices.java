package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.core.VeraPDFException;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ParseResult2Xml;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validators.Validators;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
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

    public static void main(String[] args) throws Exception {
        File pdfFile = new File("/home/mmj/projects/digital-pligtaflevering-aviser-tools/main/dpa-verapdf/src/test/resources/veraPDF test suite 6-8-t02-pass-a.pdf");
        VeraPDFWebServices veraPDFWebServices = new VeraPDFWebServices("http://localhost:8080/api");
        System.out.println(veraPDFWebServices.getIds());
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  PDFAFlavour.byFlavourId("1b")));
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  PDFAFlavour.byFlavourId("3b")));
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  PDFAFlavour.byFlavourId("1a")));
    }


    public String validate(InputStream inputStream, PDFAFlavour profileId) throws Exception {
        ModelParser toValidate = ModelParser.createModelWithFlavour(inputStream, profileId);
        PDFAValidator validator = Validators.createValidator(profileId, false);
        ValidationResult result = validator.validate(toValidate);
        return ParseResult2Xml.convert2Xml(result);
    }

}
