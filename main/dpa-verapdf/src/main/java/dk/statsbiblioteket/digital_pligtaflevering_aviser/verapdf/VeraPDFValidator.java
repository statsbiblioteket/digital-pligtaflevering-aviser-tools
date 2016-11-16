package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.core.XmlSerialiser;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;

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
        PDFAFlavour profileId = PDFAFlavour.byFlavourId(flavorId);
        ModelParser toValidate = ModelParser.createModelWithFlavour(inputStream, profileId);
        PDFAValidator validator = ValidatorFactory.createValidator(profileId, false);
        ValidationResult result = validator.validate(toValidate);

        // as of 0.25 ValidationResult can be marshalled using JAXB to XML
        // for now do in-memory generation of XML byte array - as we need to pass it to Fedora we need it to fit in memory anyway.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XmlSerialiser.toXml(result, baos, prettyXml, false);
        return baos.toByteArray();
    }
}

