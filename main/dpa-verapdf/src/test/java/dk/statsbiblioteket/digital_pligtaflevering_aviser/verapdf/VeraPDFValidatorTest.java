package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class VeraPDFValidatorTest {
    public static final String PDF1B_SUCCESS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<validationResult flavour=\"PDFA_1_B\" totalAssertions=\"250\" isCompliant=\"true\" xmlns=\"http://www.verapdf.org/ValidationProfile\">" +
            "<assertions/>" +
            "</validationResult>";
    private VeraPDFValidator validator1b;
    private VeraPDFValidator validator1a;

    @Before
    public void setUp() throws Exception {
        validator1b = new VeraPDFValidator("1b", false);
        validator1a = new VeraPDFValidator("1a", false);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test6_8_2_2_t01_fail_a() {
        assertEquals(PDF1B_SUCCESS, validateResource1b("/veraPDF test suite 6-8-2-2-t01-fail-a.pdf"));
        final String actualResponse = validateResource1a("/veraPDF test suite 6-8-2-2-t01-fail-a.pdf");
        // For some reason the number in <code><assertion ordinal="..."</code> varies depending on context.
        // For now, do a simple replacement on string level to get a passable test.

        String fixedResponse = actualResponse.replaceFirst("<assertion ordinal=\"\\d+\"", "**DELETED**");

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<validationResult flavour=\"PDFA_1_A\" totalAssertions=\"254\" isCompliant=\"false\" xmlns=\"http://www.verapdf.org/ValidationProfile\">" +
                "<assertions>" +
                // "<assertion ordinal=\"10\" status=\"FAILED\">" +
                "**DELETED** status=\"FAILED\">" +
                "<ruleId specification=\"ISO_19005_1\" clause=\"6.8.2\" testNumber=\"1\"/>" +
                "<message>The document catalog dictionary shall include a MarkInfo dictionary whose sole entry, Marked, shall have a value of true</message>" +
                "<location>" +
                "<level>CosDocument</level>" +
                "<context>root</context>" +
                "</location>" +
                "</assertion>" +
                "</assertions>" +
                "</validationResult>", fixedResponse);
    }

    private String validateResource1b(String resourceName) {
        InputStream is = getClass().getResourceAsStream(resourceName);
        assertNotNull(is);
        String result = validator1b.apply(is);
        assertNotNull(result);
        return result;
    }

    private String validateResource1a(String resourceName) {
        InputStream is = getClass().getResourceAsStream(resourceName);
        assertNotNull(is);
        String result = validator1a.apply(is);
        assertNotNull(result);
        return result;
    }
}
