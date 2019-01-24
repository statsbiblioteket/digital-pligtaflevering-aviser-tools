package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.JaxbList;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.PdfContentUtils;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Test af xml validation against xsd.
 * This component uses two different xsd-files Article.xsd and PdfInfo.xsd.
 * It looks for the rootTag in the xml, and finds the schema expected to match the name of the rootTag
 */
public class ValidateXMLOfEmbeddedFiles {

    @org.junit.Test
    public void analyzeXmlForFileList() throws Exception {

        ArrayList<String> files = new ArrayList<String>();
        files.add("embeddedFile1");
        files.add("embeddedFile2");
        JaxbList jaxbList = new JaxbList(files);

        String jaxbStream = PdfContentUtils.marshallListOfEmbeddedFilesInfo(jaxbList).toString();
        JaxbList newJaxbList = PdfContentUtils.getListOfEmbeddedFilesFromXml(jaxbStream);

        assertEquals("Failed Files : ", "embeddedFile1", newJaxbList.getList().get(0));
        assertEquals("Failed Files : ", "embeddedFile2", newJaxbList.getList().get(1));
    }
}
