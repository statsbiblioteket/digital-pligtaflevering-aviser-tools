package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import dk.kb.stream.StreamTuple;
import io.vavr.control.Try;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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

/**
 * Validator for checking the results from VeraPDF
 * The results is validated agains the recommodations from "Digital bevarings gruppen"
 * https://sbprojects.statsbiblioteket.dk/display/DPAA/Projektdokumenter?preview=/15993252/31490419/pdfa-anbefaling.pdf
 */
public class VeraPDFOutputValidation {

    public static final String REMOVE_NEWLINES_REGEX = "[ \t]*\n[ \t]+";

    String expression;

    public VeraPDFOutputValidation(boolean mrrFormat) {
        // FIXME:  We must also check that the rule specification is the one we expect.
        if (mrrFormat) {
            expression = "/report/jobs/job/validationReport/details/rule[@status='failed']/@clause";
        } else {
            expression = "//validationResult/assertions/assertion[@status='FAILED']/ruleId/@clause";
        }
    }

    /**
     * Get report of pdf-failures. It does not take the embedded files into account
     * @param xmlDocument
     * @return
     */
    public static String getReportFor(Document xmlDocument) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = null;
        final String expression = "//status[text() = 'FAILED']/..";
        try {
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(expression, e);
        }

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
                                ("" + xPath.compile(
                                        "concat(ruleId/clause/text(), ': ', message/text(), ' [ ', location/context/text(), ' ]')"
                                ).evaluate(node, XPathConstants.STRING)).replaceAll(REMOVE_NEWLINES_REGEX, " ")
                        )).get())
                .collect(groupingBy(st -> severenessFor(st.left()), mapping(st -> st, toList())));

        Optional<SeverenessLevel> worstOutcome = outcomes.keySet().stream().max(Comparator.naturalOrder());

        return outcomes.entrySet().stream()
                .peek((Map.Entry<SeverenessLevel, List<StreamTuple<String, String>>> s) -> {
                })
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey() + "\n"
                        + "------------------------------------------\n"
                        + entry.getValue().stream()
                        .collect(groupingBy(Function.identity(), counting())).entrySet().stream()
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map((Map.Entry<StreamTuple<String, String>, Long> entry2) ->
                                entry2.getValue() + ": " + entry2.getKey().right())
                        .collect(joining("\n"))
                )
                .collect(joining("\n\n"));
    }

    /**
     * Supply with output from VeraPDF
     * The stream is parsed and delivered into a Set, in this way every rulebreak is only handled once
     *
     * @param is Inputstream from VeraPDF
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public List<String> extractRejected(InputStream is) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        List<String> result = new ArrayList<>();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(is);

        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

        for (int index = 0; index < nodeList.getLength(); index++) {
            String rule = nodeList.item(index).getNodeValue();
            if (rule != null) {
                result.add(nodeList.item(index).getNodeValue());
            }
        }
        return result;
    }

    /**
     * Take a list of broken rules in a pdf-file, and return a list af the rules that is "bad" according to 'SeverenessLevel'
     *
     * @return ValidationResults indication the result of validating output from VeraPDF
     */
    public ValidationResults validateResult(List<String> l) {

        ValidationResults rulesBroken = new ValidationResults();

        new HashSet<>(l).stream()
                .map(item -> new ValidationResult(item, severenessFor(item)))
                .filter(vr -> vr.getValidationEnum() != SeverenessLevel.ACCEPTABLE)
                .forEach(vr -> rulesBroken.add(vr));

        return rulesBroken;
    }

    public static SeverenessLevel severenessFor(String sectionId, List<String> embeddedFiles) {
        if("6.1.11".equals(sectionId) && embeddedFiles!=null
                && embeddedFiles.stream().filter(embeddedFile -> embeddedFile.contains(".joboptions")).count()>0) {
            //Information always deliveres pdf-files with embedded files named@@@@@@.joboptions.
            //We have decided to accept thease file, and we hereby ignores 6.1.11 when they contains this file
            return SeverenessLevel.ACCEPTIGNORE;
        } else {
            return severenessFor(sectionId);
        }
    }


    public static SeverenessLevel severenessFor(String sectionId) {
        //Errorcases has been updated according to this document:
        //https://sbprojects.statsbiblioteket.dk/pages/viewpage.action?pageId=15993274
        switch (sectionId) {
            case "6.1.3":
            case "6.5.2":
            case "6.6.1":
            case "6.6.2":
            case "6.9":
                return SeverenessLevel.INVALID;
            case "6.1.11":
            case "6.2.6":
            case "6.3.4":
            case "6.3.2":
                return SeverenessLevel.MANUAL_INSPECTION;
            case "6.1.7":
            case "6.3.6":
            case "6.3.5":
            case "6.3.3.1":
            case "6.2.10":
            case "6.1.2":
            case "6.1.4":
            case "6.1.6":
            case "6.1.8":
            case "6.1.12":
            case "6.1.13":
            case "6.2.4":
            case "6.2.5":
            case "6.5.3":
            case "6.2.7":
            case "6.1.10":
            case "6.2.2":
            case "6.2.8":
            case "6.2.9":
            case "6.3.3.2":
            case "6.3.7":
            case "6.7.10":
            case "6.4":
            case "6.1.5":
                return SeverenessLevel.ACCEPTABLE;
            default:
                if (sectionId.startsWith("6.2.3") || sectionId.startsWith("6.7") || sectionId.startsWith("6.8")) {
                    return SeverenessLevel.ACCEPTABLE;
                } else {
                    return SeverenessLevel.UNKNOWN;
                }
        }
    }
}

