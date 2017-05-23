package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Parser for parsing between DomsItem, DeliveryStatistics an an xml-based bytestream
 */
public class DomsParser {

    /**
     * Find all childItems from a DomsItem
     * @return
     */
    public Function<DomsItem, Iterator<DomsItem>> processChildDomsId() {
        return domsItem -> {
            Stream<DomsItem> domsChildren =domsItem.children();
            Iterator<DomsItem> itemIterator = domsChildren.iterator();
            return itemIterator;
        };
    }

    /**
     * initiate a DeliveryStatistics based on a DomsItem in doms
     * @return DeliveryStatistics
     */
    public Function<DomsItem, DeliveryStatistics> processDomsIdToStream() {
        return domsItem -> {

            Iterator<DomsItem> roundtripIterator = this.processChildDomsId().apply(domsItem);

            DeliveryStatistics deliveryStatistics = new DeliveryStatistics();
            deliveryStatistics.setDeliveryName(domsItem.getPath());

            while (roundtripIterator.hasNext()) {

                DomsItem titleItem = roundtripIterator.next();
                Iterator<DomsItem> titleSubfolder = this.processChildDomsId().apply(titleItem);
                String titleName = titleItem.getPath();
                Title title = new Title(titleName.substring(titleName.indexOf("/") + 1));
                deliveryStatistics.addTitle(title);

                while (titleSubfolder.hasNext()) {
                    DomsItem titleSubfolderItem = titleSubfolder.next();
                    Stream<DomsItem> fileStream = titleSubfolderItem.children();
                    Iterator<DomsItem> fileItemIterator = fileStream.iterator();
                    while (fileItemIterator.hasNext()) {
                        DomsItem fileItem = fileItemIterator.next();
                        String titleSubfolderItemName = titleSubfolderItem.getPath();
                        String fileItemName = fileItem.getPath();
                        final List<DomsDatastream> datastreams = fileItem.datastreams();
                        Optional<DomsDatastream> profileOptional = datastreams.stream()
                                .filter(ds -> ds.getId().equals("XML"))
                                .findAny();

                        if (profileOptional.isPresent()) {
                            try {
                                DomsDatastream ds = profileOptional.get();
                                //We are reading this textstring as a String and are aware that thish might leed to encoding problems
                                StringReader reader = new StringReader(ds.getDatastreamAsString());
                                InputSource inps = new InputSource(reader);

                                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                                Document xmlDocument = builder.parse(inps);

                                String sectionName = getSectionName(xmlDocument);
                                String sectionNumber = getSectionNumber(xmlDocument);
                                String pageNumber = getPageNumber(xmlDocument);
                                String id = fileItem.getDomsId().id();

                                //fileItemName = fileItemName.substring(fileItemName.lastIndexOf("/"));
                                if (titleSubfolderItemName.contains("articles")) {
                                    title.addArticle(new Article(id, fileItemName, sectionName, sectionNumber, pageNumber));
                                } else if (titleSubfolderItemName.contains("pages")) {
                                    title.addPage(new Page(id, fileItemName, sectionName, sectionNumber, pageNumber));
                                }
                            } catch (Exception e) {
                                return null;
                            }

                        }
                    }
                }
            }

            return deliveryStatistics;
        };
    }

    /**
     * Find the sectionName inside metadata of an xml-file descriping a page
     * @param xmlDocument
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public String getSectionName(Document xmlDocument) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//sectionname");
        String sectionName = expr.evaluate(xmlDocument, XPathConstants.STRING).toString();
        return sectionName;
    }

    /**
     * Find the sectionNumber inside a document
     * @param xmlDocument
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public String getSectionNumber(Document xmlDocument) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//sectionnumber");
        String sectionName = expr.evaluate(xmlDocument, XPathConstants.STRING).toString();
        return sectionName;
    }

    /**
     * Find the pagename inside a document
     * @param xmlDocument
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public String getPageNumber(Document xmlDocument) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//pagenumber");
        String sectionName = expr.evaluate(xmlDocument, XPathConstants.STRING).toString();
        return sectionName;
    }


    /**
     * convert DeliveryStatistics into a bytseArray which can be saved in doms
     * @return
     */
    public Function<DeliveryStatistics, byte[]> processDeliveryStatisticsToBytestream() {
        return domsItem -> {
            ByteArrayOutputStream deliveryArrayStream = new ByteArrayOutputStream();
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(domsItem, deliveryArrayStream);
            } catch (Exception e) {
                return null;
            }
            return deliveryArrayStream.toByteArray();
        };
    }
}