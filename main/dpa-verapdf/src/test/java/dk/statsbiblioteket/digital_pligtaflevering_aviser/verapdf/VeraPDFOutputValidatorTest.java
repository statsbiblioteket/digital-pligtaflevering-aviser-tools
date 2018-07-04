package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import dk.kb.stream.StreamTuple;
import io.vavr.control.Try;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

/**
 * Unittest for validating the behavior of the parsing of results from VeraPDF
 */
public class VeraPDFOutputValidatorTest {

    private Document xmlDocument;
    private VeraPDFOutputValidation validator;

    @Before
    public void setUp() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/VERAPDF-REST-20180702.xml")) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            xmlDocument = builder.parse(is);
        }
        validator = new VeraPDFOutputValidation(false);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRestOutput1() throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile("//status[text() = 'FAILED']/..").evaluate(xmlDocument, XPathConstants.NODESET);

        /*
         <testAssertions>
            <ordinal>3446</ordinal>
            <ruleId>
                <specification>ISO_19005_1</specification>
                <clause>6.2.3</clause>
                <testNumber>4</testNumber>
            </ruleId>
            <status>FAILED</status>
            <message>If an uncalibrated colour space is used in a file then that file shall contain a PDF/A-1
        OutputIntent, as defined in 6.2.2
                </message>
            <location>
                <level>CosDocument</level>
                <context>root/document[0]/pages[0](4 0 obj PDPage)/contentStream[0]/operators[4138]/fillCS[0]</context>
            </location>
        </testAssertions>
        */

        // https://stackoverflow.com/a/23361853/53897

        Map<SeverenessLevel, List<StreamTuple<String, String>>> outcomes = IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .map((Node node) ->
                        Try.of(() -> new StreamTuple<>(
                                "" + xPath.compile("ruleId/clause/text()").evaluate(node, XPathConstants.STRING),
                                "" + xPath.compile(
                                        "concat(ruleId/clause/text(), ': ', message/text(), ' [ ', location/context/text(), ' ]')"
                                ).evaluate(node, XPathConstants.STRING)
                        )).get())
                .collect(groupingBy(st -> validator.severenessFor(st.left()), mapping(st -> st, toList())));

        Optional<SeverenessLevel> worstOutcome = outcomes.keySet().stream().max(Comparator.naturalOrder());

        String thisOutcome = outcomes.entrySet().stream()
                .peek((Map.Entry<SeverenessLevel, List<StreamTuple<String, String>>> s) -> {
                })
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey() + "\n"
                        + "------------------------------------------\n"
                        + entry.getValue().stream()
                        .collect(groupingBy(Function.identity(), counting())).entrySet().stream()
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map((Map.Entry<StreamTuple<String, String>, Long> entry2) ->
                                entry2.getValue() + ": " + entry2.getKey().right().replaceAll("[ \t]*\n[ \t]+", " "))
                        .collect(joining("\n"))
                )
                .collect(joining("\n\n"));

        System.out.println(thisOutcome);

    }

    /**
     * Test against an output with 6 broken rules, one of them is UNKNOWN
     *
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlManyBrokenRule() throws Exception {

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(false);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/ManyImportantBrokenRules.xml"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", SeverenessLevel.INVALID, validationResults.getWorstBrokenRule());
        assertEquals("Unexpected validation", 6, validationResults.getRulesBroken().size());
        int invalid = 0;
        int unknown = 0;
        int manual = 0;

        for (ValidationResult vr : validationResults.getRulesBroken()) {

            if (vr.getValidationEnum() == SeverenessLevel.INVALID) {
                invalid++;
            } else if (vr.getValidationEnum() == SeverenessLevel.UNKNOWN) {
                unknown++;
            } else if (vr.getValidationEnum() == SeverenessLevel.MANUAL_INSPECTION) {
                manual++;
            }
        }
        assertEquals("Unexpected validation", 3, invalid);
        assertEquals("Unexpected validation", 1, unknown);
        assertEquals("Unexpected validation", 2, manual);
    }

    /**
     * Test of an output with no broken rules
     *
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlNoBrokenRule() throws Exception {

        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(false);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/NoImportantBrokenRules.xml"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", SeverenessLevel.ACCEPTABLE, validationResults.getWorstBrokenRule());
    }

    /**
     * Test of an output with one broken rule
     *
     * @throws Exception
     */
    @Test
    public void testResultValidationXmlOneBrokenRule() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(false);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/OneManualInspectRule.xml"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", SeverenessLevel.MANUAL_INSPECTION, validationResults.getWorstBrokenRule());
        assertEquals("Unexpected validation", 1, validationResults.getRulesBroken().size());

    }

    /**
     * Test of parsing of an mrr output
     *
     * @throws Exception
     */
    @Test
    public void testResultValidationMrr() throws Exception {
        VeraPDFOutputValidation rulo = new VeraPDFOutputValidation(true);
        List<String> ids = rulo.extractRejected(getClass().getResourceAsStream("/test.mrr"));
        ValidationResults validationResults = rulo.validateResult(ids);
        assertEquals("Unexpected validation", SeverenessLevel.INVALID, validationResults.getWorstBrokenRule());
    }
}
