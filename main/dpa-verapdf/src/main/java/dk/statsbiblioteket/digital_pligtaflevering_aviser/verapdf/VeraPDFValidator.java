package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.core.XmlSerialiser;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Function;

/**
 *
 */
public class VeraPDFValidator implements Function<InputStream, byte[]> {
    private String flavorId;
    private Boolean prettyXml;

    /**
     * VeraPDF can validate against several PDF "flavors".  A list of supported values can be retrieved from
     * "PDFAFlavour.getFlavourIds()".  Returns a string on XML form of issues detected.
     *
     * @param flavorId  what to validate against - e.g. "1b"
     * @param prettyXml should XML be pretty printed - if not a single line is returned.
     */
    public VeraPDFValidator(String flavorId, Boolean prettyXml) {
        this.flavorId = flavorId;
        this.prettyXml = prettyXml;

        // tell verapdf we want the greenfield parser.
        VeraGreenfieldFoundryProvider.initialise();
    }

    @Override
    public byte[] apply(InputStream inputStream) {
        try {
            return apply0(inputStream);
        } catch (RuntimeException e) {
            throw e;
        } catch (ModelParsingException | ValidationException | JAXBException | EncryptedPdfException e) {
            throw new RuntimeException("invoking VeraPDF validation", e);
        }
    }

    private byte[] apply0(InputStream inputStream) throws ModelParsingException, ValidationException, JAXBException, EncryptedPdfException {
        /*
        Carl Wilson 2016-12-29: The best place to look for an example is here:

        https://github.com/veraPDF/veraPDF-integration-tests/blob/integration/src/test/java/org/verapdf/integration/CorpusTest.java#L60

        The appropriate Maven include is here:

        https://github.com/veraPDF/veraPDF-integration-tests/blob/integration/pom.xml#L83.

        TRA 2017-01-23:  Froze pom.xml ranges at version 1.0.6
         */
        PDFAFlavour flavour = PDFAFlavour.byFlavourId(flavorId);
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);
        PDFAParser loader = Foundries.defaultInstance().createParser(inputStream, flavour);
        ValidationResult result = validator.validate(loader);

        // do in-memory generation of XML byte array - as we need to pass it to Fedora we need it to fit in memory anyway.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlSerialiser.toXml(result, baos, prettyXml, false);
        final byte[] byteArray = baos.toByteArray();
        return byteArray;
    }
}

