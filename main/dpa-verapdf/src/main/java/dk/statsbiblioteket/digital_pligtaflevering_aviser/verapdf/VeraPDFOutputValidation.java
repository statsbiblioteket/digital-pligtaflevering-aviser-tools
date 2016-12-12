package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.w3c.dom.Document;
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
import java.util.HashSet;
import java.util.Iterator;

/**
 * Validator for checking the results from VeraPDF
 * The results is validated agains the recommodations from "Digital bevarings gruppen"
 * https://sbprojects.statsbiblioteket.dk/display/DPAA/Projektdokumenter?preview=/15993252/31490419/pdfa-anbefaling.pdf
 */
public class VeraPDFOutputValidation {

    private final String xmlParserString = "//validationResult/assertions/assertion[@status='FAILED']/ruleId/@clause";
    private final String mrrParserString = "/report/jobs/job/validationReport/details/rule[@status='failed']";
    private HashSet<String> rejections = new HashSet<String>();

    /**
     * Supply with output from VeraPDF
     * @param is Inputstream from VeraPDF
     * @param mrrFormat True if the format is the mrr-output
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public VeraPDFOutputValidation(InputStream is, boolean mrrFormat) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        builder = builderFactory.newDocumentBuilder();
        XPath xPath = XPathFactory.newInstance().newXPath();

        Document xmlDocument = builder.parse(is);
        String expression;

        if(mrrFormat) {
            expression = mrrParserString;
        } else {
            expression = xmlParserString;
        }

        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

        for(int index = 0; index < nodeList.getLength(); index++) {
            String rule = nodeList.item(index).getNodeValue();
            if(rule != null) {
                rejections.add(nodeList.item(index).getNodeValue());
            }
        }
    }

    /**
     * Validate the results of paragraphs in a pdf, the broken rules is delivered in a set
     * The first time
     * @return ValidationResults indication the result of validating output from VeraPDF
     */
    public ValidationResults validateResult() {

        ValidationResults rulesBroken = new ValidationResults();
        Iterator<String> it = rejections.iterator();

        while(it.hasNext()) {

            String item = it.next();

            switchCondition : switch(item) {
                case "6.1.3":
                case "6.5.2":
                case "6.6.1":
                case "6.6.2":
                case "6.9":
                    rulesBroken.add(new ValidationResult(item, ValidationResult.ValidationResultEnum.invalid));
                    break switchCondition;
                case "6.1.7":
                case "6.1.11":
                case "6.2.6":
                case "6.3.4":
                case "6.3.5":
                case "6.3.3.1":
                case "6.3.6":
                case "6.2.10":
                case "6.3.2":
                    rulesBroken.add(new ValidationResult(item, ValidationResult.ValidationResultEnum.manualInspection));
                    break switchCondition;

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
                    break switchCondition;// Do nothing, this is acceptes look for the next one

                default:
                    // If the paragraph is not found in any of the above, look for paragraphes starting with the following, thease paragraphes is ignored.
                    if(item.startsWith("6.2.3") || item.startsWith("6.7") || item.startsWith("6.8")) {
                        break switchCondition;// Do nothing, this is accepted look for the next one
                    } else {
                        // If a paragraph is not recognized return unknown to make sure that the system informs about paragraphes that has not been decided about
                        rulesBroken.add(new ValidationResult(item, ValidationResult.ValidationResultEnum.unknown));
                    }
            }
        }
        return rulesBroken;
    }
}