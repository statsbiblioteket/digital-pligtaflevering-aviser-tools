package org.statsbiblioteket.digital_pligtaflevering_aviser.ui;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.maven.MavenProjectsHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Runner for stating the application during development
 */
public class JettyRunner {

    public static void main(String[] args) throws Exception {

        // Create Jetty Server
        Server server = new Server(8080);

        Path warPath = MavenProjectsHelper.getRequiredPathTowardsRoot(NewspaperUI.class, "dpa-manualcontrol.war");
        Path xmlPath = MavenProjectsHelper.getRequiredPathTowardsRoot(NewspaperUI.class, "dpa-manualcontrol_jetty.xml");

        InputStream in = new FileInputStream(xmlPath.toFile());
        InputSource inputSource = new InputSource(new InputStreamReader(in, StandardCharsets.UTF_8));

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/dpa-manualcontrol");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(inputSource);
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//Parameter");
        NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < nl.getLength(); i++) {
            String paramName = nl.item(i).getAttributes().getNamedItem("name").getTextContent();
            String paramValue = nl.item(i).getAttributes().getNamedItem("value").getTextContent();
            webapp.setInitParameter(paramName, paramValue);
        }

        webapp.setWar(warPath.toString());
        server.setHandler(webapp);

        server.start();
        server.join();

        server.dumpStdErr();
    }
}
