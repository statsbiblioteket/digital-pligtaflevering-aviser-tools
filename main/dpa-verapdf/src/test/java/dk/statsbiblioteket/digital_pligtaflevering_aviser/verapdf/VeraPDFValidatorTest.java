package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

import static junit.framework.TestCase.assertNotNull;

/**
 *
 */
public class VeraPDFValidatorTest {
    public static final String PDF1B_SUCCESS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<validationResult xmlns=\"http://www.verapdf.org/ValidationProfile\" flavour=\"PDFA_1_B\" totalAssertions=\"250\" isCompliant=\"true\">" +
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
        final String actualResponseb = validateResource1b("/veraPDF test suite 6-8-2-2-t01-fail-a.pdf");
        Assert.assertThat(actualResponseb, CoreMatchers.containsString("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
        Assert.assertThat(actualResponseb, CoreMatchers.containsString("flavour=\"PDFA_1_B\""));
        Assert.assertThat(actualResponseb, CoreMatchers.containsString("isCompliant=\"true\""));

        final String actualResponsea = validateResource1a("/veraPDF test suite 6-8-2-2-t01-fail-a.pdf");
        // For some reason the number in <code><assertion ordinal="..."</code> varies depending on context.
        // For now, do a simple replacement on string level to get a passable test.

        Assert.assertThat(actualResponsea, CoreMatchers.containsString("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
        Assert.assertThat(actualResponsea, CoreMatchers.containsString("flavour=\"PDFA_1_A\""));
        Assert.assertThat(actualResponsea, CoreMatchers.containsString("isCompliant=\"false\""));
        Assert.assertThat(actualResponsea, CoreMatchers.containsString("<message>The document catalog dictionary shall include a MarkInfo dictionary with a Marked entry in it, whose value shall be true.</message>"));
        Assert.assertThat(actualResponsea, CoreMatchers.containsString("specification=\"ISO_19005_1\""));
        Assert.assertThat(actualResponsea, CoreMatchers.containsString("clause=\"6.8.2\""));
        Assert.assertThat(actualResponsea, CoreMatchers.containsString("testNumber=\"1\"/>"));

    }

    private String validateResource1b(String resourceName) {
        InputStream is = getClass().getResourceAsStream(resourceName);
        assertNotNull(is);
        String result = new String(validator1b.apply(is), Charset.forName("UTF-8"));
        assertNotNull(result);
        return result;
    }

    private String validateResource1a(String resourceName) {
        InputStream is = getClass().getResourceAsStream(resourceName);
        assertNotNull(is);
        String result = new String(validator1a.apply(is), Charset.forName("UTF-8"));
        assertNotNull(result);
        return result;
    }
}
