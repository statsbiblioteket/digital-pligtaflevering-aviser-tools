package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Unittest for validating the behavior of the parsing of results from VeraPDF
 */
public class VeraPDFOutputValidatorTest {



    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Test against an output with 6 broken rules, one of them is UNKNOWN
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlManyBrokenRule() throws Exception {

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(false);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/ManyImportantBrokenRules.xml"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.INVALID, validationResults.getWorstBrokenRule());
        assertEquals("Unexpected validation", 6, validationResults.getRulesBroken().size());
        int invalid = 0;
        int unknown = 0;
        int manual = 0;

        for(ValidationResult vr : validationResults.getRulesBroken()) {

            if(vr.getValidationEnum() == ValidationResult.ValidationResultEnum.INVALID) {
                invalid++;
            } else if(vr.getValidationEnum() == ValidationResult.ValidationResultEnum.UNKNOWN) {
                unknown++;
            } else if(vr.getValidationEnum() == ValidationResult.ValidationResultEnum.MANUAL_INSPECTION) {
                manual++;
            }
        }
        assertEquals("Unexpected validation", 3, invalid);
        assertEquals("Unexpected validation", 1, unknown);
        assertEquals("Unexpected validation", 2, manual);
    }

    /**
     * Test of an output with no broken rules
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlNoBrokenRule() throws Exception {

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(false);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/NoImportantBrokenRules.xml"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.ACCEPTABLE, validationResults.getWorstBrokenRule());
    }

    /**
     * Test of an output with one broken rule
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlOneBrokenRule() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(false);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/OneManualInspectRule.xml"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.MANUAL_INSPECTION, validationResults.getWorstBrokenRule());
        assertEquals("Unexpected validation", 1, validationResults.getRulesBroken().size());

    }

    /**
     * Test of parsing of an mrr output
     * @throws Exception
     */
    @Test
    public void testResultValidationMrr() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(true);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/test.mrr"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.INVALID, validationResults.getWorstBrokenRule());
    }
}
