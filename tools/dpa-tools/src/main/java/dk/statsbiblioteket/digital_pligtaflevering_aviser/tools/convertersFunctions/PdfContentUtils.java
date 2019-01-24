package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * MarshallerFunctions is a list of static functions which converts between objects .......
 */
public class PdfContentUtils {
    //XML example
    //<ns2:fileList xmlns:ns2="kb.dk/dpa/embeddedfiles">
    //<EmbeddedItems xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">NewspaperAds_1v4.joboptions</EmbeddedItems>
    //</ns2:fileList>
    
    protected static Logger log = LoggerFactory.getLogger(PdfContentUtils.class);
    public final static JAXBContext jaxbContext;
    
    static {
        try {
            jaxbContext = JAXBContext.newInstance(JaxbList.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to create JAXBContext",e);
        }
    }
    
    /**
     * Get the serializable JaxbList from xml
     * @param xml
     * @return
     *
     * @throws JAXBException
     */
    public static JaxbList<String> getListOfEmbeddedFilesFromXml(String xml) throws JAXBException {
        try (StringReader reader = new StringReader(xml)) {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JaxbList<String> deserializedObject = (JaxbList<String>) jaxbUnmarshaller.unmarshal(reader);
            return deserializedObject;
        }
    }

    /**
     * Get the xml-bytestream from the serializable jaxbList
     * @param jaxbList
     * @return
     * @throws JAXBException
     */
    public static ByteArrayOutputStream marshallListOfEmbeddedFilesInfo(JaxbList jaxbList) throws JAXBException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(jaxbList, os);
        return os;
    }


    /**
     * Return function for the xml-bytestream from the serializable jaxbList
     * @return
     */
    public static Function<JaxbList, byte[]> processListOfEmbeddedFilesToBytestream() {
        return deliveryStatistics -> {
            try {
                return marshallListOfEmbeddedFilesInfo(deliveryStatistics).toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Failed to transform xml",e);
            }
        };
    }

    /**
     * Get a list of embedded files inside a pdf-file
     * @param pathToPdf
     * @return
     * @throws IOException
     */
    public static List<String> getListOfEmbeddedFilesFromPdf(URL pathToPdf) throws IOException {
        try (final PDDocument document = PDDocument.load(pathToPdf.openStream())) {
            PDDocumentNameDictionary namesDictionary = new PDDocumentNameDictionary(document.getDocumentCatalog());

            PDEmbeddedFilesNameTreeNode efTree = namesDictionary.getEmbeddedFiles();
            ArrayList<String> returnList = new ArrayList<String>();
            if(efTree!=null) {
                List<PDNameTreeNode<PDComplexFileSpecification>> tt = efTree.getKids();//PDEmbeddedFilesNameTreeNode
                for(PDNameTreeNode node : tt) {
                    returnList.addAll(node.getNames().keySet());
                }
            }
            return returnList;
        }
    }
}
