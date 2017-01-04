package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import static org.junit.Assert.*;

import com.helger.commons.collection.ext.ICommonsList;
import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;
import com.helger.schematron.svrl.SVRLFailedAssert;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.svrl.SVRLSuccessfulReport;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * CSchematron validation
 */
public class ValidateSchematronXMLMainTest {


    @org.junit.Test
    public void analyzeArticleAgainstCommonSchemaTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("schematronValidation/articleCorrectTest.xml");

        final ISchematronResource schematronSchemaCommonExec = SchematronResourcePure.fromClassPath("schematronValidation/Common.sch");
        if(!schematronSchemaCommonExec.isValidSchematron()) {
            assertEquals("INVALID", false, true);
        }

        SchematronResourcePure schematronSchemaDescriptive = new SchematronResourcePure(schematronSchemaCommonExec.getResource());

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        schematronSchemaDescriptive.applySchematronValidation(new StreamSource(in));
        in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        SchematronOutputType results = schematronSchemaDescriptive.applySchematronValidationToSVRL(new StreamSource(in));

        final ICommonsList<SVRLSuccessfulReport> failedReports = SVRLHelper.getAllSuccessfulReports(results);
        final ICommonsList<SVRLFailedAssert> failedAssertions = SVRLHelper.getAllFailedAssertions(results);

        assertEquals("FAILING ASSERTIONS", 0, failedAssertions.size());
        assertEquals("FAILING REPORTS", 0, failedReports.size());
    }

    @org.junit.Test
    public void analyzeAPageAgainstCommonASchemaTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("schematronValidation/pageCorrectTest.xml");

        final ISchematronResource schematronSchemaCommonExec = SchematronResourcePure.fromClassPath("schematronValidation/Common.sch");
        if(!schematronSchemaCommonExec.isValidSchematron()) {
            assertEquals("INVALID", false, true);
        }

        SchematronResourcePure schematronSchemaDescriptive = new SchematronResourcePure(schematronSchemaCommonExec.getResource());

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        schematronSchemaDescriptive.applySchematronValidation(new StreamSource(in));
        in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        SchematronOutputType results = schematronSchemaDescriptive.applySchematronValidationToSVRL(new StreamSource(in));

        final ICommonsList<SVRLSuccessfulReport> failedReports = SVRLHelper.getAllSuccessfulReports(results);
        final ICommonsList<SVRLFailedAssert> failedAssertions = SVRLHelper.getAllFailedAssertions(results);

        assertEquals("FAILING ASSERTIONS", 0, failedAssertions.size());
        assertEquals("FAILING REPORTS", 0, failedReports.size());
    }

    @org.junit.Test
    public void analyzeAcceptedXMLArticleTest() throws Exception {

        URL xmlurl = getClass().getClassLoader().getResource("schematronValidation/articleCorrectTest.xml");

        final ISchematronResource schematronSchemaCommonExec = SchematronResourcePure.fromClassPath("schematronValidation/Article.sch");
        if(!schematronSchemaCommonExec.isValidSchematron()) {
            assertEquals("INVALID", false, true);
        }

        SchematronResourcePure schematronSchemaDescriptive = new SchematronResourcePure(schematronSchemaCommonExec.getResource());
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        schematronSchemaDescriptive.applySchematronValidation(new StreamSource(in));
        in = new BufferedReader(new InputStreamReader(new FileInputStream(xmlurl.getFile()), "UTF8"));
        SchematronOutputType results = schematronSchemaDescriptive.applySchematronValidationToSVRL(new StreamSource(in));

        final ICommonsList<SVRLSuccessfulReport> failedReports = SVRLHelper.getAllSuccessfulReports(results);
        final ICommonsList<SVRLFailedAssert> failedAssertions = SVRLHelper.getAllFailedAssertions(results);

        assertEquals("FAILING ASSERTIONS", 0, failedAssertions.size());
        assertEquals("FAILING REPORTS", 0, failedReports.size());
    }
}