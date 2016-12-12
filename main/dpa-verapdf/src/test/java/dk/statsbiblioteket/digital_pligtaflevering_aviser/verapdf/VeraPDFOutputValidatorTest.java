package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
     * Test against an output with 6 broken rules, one of them is unknown
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlManyBrokenRule() throws Exception {

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/ManyImportantBrokenRules.xml"), false);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.invalid, validationResults.getWorstBrokenRule());
        assertEquals("Unexpected validation", 6, validationResults.getRulesBroken().size());
        int invalid = 0;
        int unknown = 0;
        int manual = 0;

        for(ValidationResult vr : validationResults.getRulesBroken()) {

            if(vr.getValidationEnum() == ValidationResult.ValidationResultEnum.invalid) {
                invalid++;
            } else if(vr.getValidationEnum() == ValidationResult.ValidationResultEnum.unknown) {
                unknown++;
            } else if(vr.getValidationEnum() == ValidationResult.ValidationResultEnum.manualInspection) {
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

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/NoImportantBrokenRules.xml"), false);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.approved, validationResults.getWorstBrokenRule());
    }

    /**
     * Test of an output with one broken rule
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlOneBrokenRule() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/OneManualInspectRule.xml"), false);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.manualInspection, validationResults.getWorstBrokenRule());
        assertEquals("Unexpected validation", 1, validationResults.getRulesBroken().size());

    }

    /**
     * Test of parsing of an mrr output
     * @throws Exception
     */
    @Test
    public void testResultValidationMrr() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/test.mrr"), true);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("Unexpected validation", ValidationResult.ValidationResultEnum.approved, validationResults.getWorstBrokenRule());
    }
}
