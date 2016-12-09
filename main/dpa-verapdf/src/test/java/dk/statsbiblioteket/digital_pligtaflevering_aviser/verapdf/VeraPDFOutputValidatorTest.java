package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
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


    @Test
    public void testResultValidationXml1() throws Exception {

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/NoImportantBrokenRules.xml"), false);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("rules has been broken", ValidationResult.ValidationResultEnum.approved, validationResults.getWorstBrokenRule());
    }

    @Test
    public void testResultValidationXml2() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/OneManualInspectRule.xml"), false);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("rules has been broken", ValidationResult.ValidationResultEnum.manualInspection, validationResults.getWorstBrokenRule());
        assertEquals("rules has been broken", 1, validationResults.getRulesBroken().size());

    }


    @Test
    public void testResultValidationMrr() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(getClass().getResourceAsStream("/test.mrr"), true);
        ValidationResults validationResults = rulo.validateResult();
        assertEquals("rules has been broken", ValidationResult.ValidationResultEnum.approved, validationResults.getWorstBrokenRule());
    }


}
