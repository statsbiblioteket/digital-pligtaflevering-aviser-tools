package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.results.ValidationResults;
import org.verapdf.pdfa.validators.Validators;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.function.Function;

/**
 *
 */
public class VeraPDFValidator implements Function<InputStream, String> {
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
    public String apply(InputStream inputStream) {
        try {
            return apply0(inputStream);
        } catch (RuntimeException e) {
            throw e;
        } catch (ModelParsingException | ValidationException | JAXBException e) {
            throw new RuntimeException("invoking VeraPDF validation", e);
        }
    }

    private String apply0(InputStream inputStream) throws ModelParsingException, ValidationException, JAXBException {
        PDFAFlavour profileId = PDFAFlavour.byFlavourId(flavorId);
        ModelParser toValidate = ModelParser.createModelWithFlavour(inputStream, profileId);
        PDFAValidator validator = Validators.createValidator(profileId, false);
        ValidationResult result = validator.validate(toValidate);

        Writer writer = new StringWriter();
        ValidationResults.toXml(result, writer, prettyXml);
        return writer.toString();
    }
}

