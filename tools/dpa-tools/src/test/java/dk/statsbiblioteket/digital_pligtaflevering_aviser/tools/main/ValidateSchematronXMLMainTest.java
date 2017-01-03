package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import static org.junit.Assert.*;

import com.helger.commons.collection.ext.ICommonsList;
import com.helger.commons.error.IResourceError;
import com.helger.commons.error.IResourceErrorGroup;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.level.IErrorLevel;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.SchematronHelper;
import com.helger.schematron.pure.SchematronResourcePure;
import com.helger.schematron.svrl.SVRLFailedAssert;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.svrl.SVRLSuccessfulReport;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CSchematron validation
 */
public class ValidateSchematronXMLMainTest {

    @org.junit.Test
    public void analyzeAcceptedXMLArticleTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("schematronValidation/articleTest.xml");




            String expression = "string-length(/article/administrativedata/articleid)";










        final ISchematronResource schematronSchemaCommonExec = SchematronResourcePure.fromClassPath("schematronValidation/Article.sch");
        if(!schematronSchemaCommonExec.isValidSchematron()) {
            assertEquals("INVALID", false, true);
        }

        SchematronResourcePure schematronSchemaDescriptive = new SchematronResourcePure(schematronSchemaCommonExec.getResource());


        String jj = schematronSchemaCommonExec.getResource().getResourceID();

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));

        schematronSchemaDescriptive.applySchematronValidation(new StreamSource(in));

        in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));

        SchematronOutputType results = schematronSchemaDescriptive.applySchematronValidationToSVRL(new StreamSource(in));


        List<String> result = new ArrayList<>();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(new FileInputStream(xmlurl.getFile()));

        XPath xPath = XPathFactory.newInstance().newXPath();
        Number nodeList = (Number) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NUMBER);



        for (Object o : results.getActivePatternAndFiredRuleAndFailedAssert()) {

            System.out.println(o);

            if (o instanceof ActivePattern) {
                ActivePattern failedAssert = (ActivePattern) o;
                String message = failedAssert.getId();
                System.out.println(message);
                if (message == null) {
                    message = "";
                }
                message = message.trim().replaceAll("\\s+", " ");
            }
        }



        IErrorList errors =SchematronHelper.convertToErrorList(results, schematronSchemaCommonExec.getResource().getResourceID());

        final ICommonsList<SVRLSuccessfulReport> failedReports = SVRLHelper.getAllSuccessfulReports(results);
        final ICommonsList<SVRLFailedAssert> failedAssertions = SVRLHelper.getAllFailedAssertions(results);



        final ICommonsList<SVRLSuccessfulReport> allSuccessfulReports3 = SVRLHelper.getAllSuccessfulReportsMoreOrEqualSevereThan(results, EErrorLevel.LOWEST);





        assertEquals("FAILING ASSERTIONS", 0, failedAssertions.size());
        assertEquals("FAILING REPORTS", 0, failedReports.size());


        System.out.println(errors);








    }

}