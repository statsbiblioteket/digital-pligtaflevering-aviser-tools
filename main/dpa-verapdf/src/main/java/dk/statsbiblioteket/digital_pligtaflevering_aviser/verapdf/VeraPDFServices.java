package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.verapdf.model.ModelParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ParseResult2Xml;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validators.Validators;

import java.io.*;
import java.util.Set;

/**
 *
 */
public class VeraPDFServices {

    public VeraPDFServices() {
    }


    public String getIds() {
        Set flavours = PDFAFlavour.getFlavourIds();
        return flavours.toString();
    }

    public static void main(String[] args) throws Exception {
        File pdfFile = new File("/home/mmj/projects/digital-pligtaflevering-aviser-tools/main/dpa-verapdf/src/test/resources/veraPDF test suite 6-8-t02-pass-a.pdf");
        VeraPDFServices veraPDFWebServices = new VeraPDFServices();
        System.out.println(veraPDFWebServices.getIds());
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  PDFAFlavour.byFlavourId("1b")));
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  PDFAFlavour.byFlavourId("3b")));
        System.out.println(veraPDFWebServices.validate(new FileInputStream(pdfFile),  PDFAFlavour.byFlavourId("1a")));
    }

    /**
     * Performe the validation of the pdf-file
     * @param inputStream
     * @param profileId
     * @return
     * @throws Exception
     */
    public String validate(InputStream inputStream, PDFAFlavour profileId) throws Exception {
        ModelParser toValidate = ModelParser.createModelWithFlavour(inputStream, profileId);
        PDFAValidator validator = Validators.createValidator(profileId, false);
        ValidationResult result = validator.validate(toValidate);
        return ParseResult2Xml.convert2Xml(result);
    }
}
