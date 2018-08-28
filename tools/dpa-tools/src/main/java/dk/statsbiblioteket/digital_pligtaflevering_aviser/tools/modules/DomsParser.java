package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
            Stream<DomsItem> domsChildren = domsItem.children();
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

                                XPathSelector xpath = DOM.createXPathSelector();
                                String sectionName = xpath.selectString(xmlDocument, "//sectionname");
                                String sectionNumber = xpath.selectString(xmlDocument, "//sectionnumber");
                                String pageNumber = xpath.selectString(xmlDocument, "//pagenumber");
                                String id = fileItem.getDomsId().id();

                                //fileItemName = fileItemName.substring(fileItemName.lastIndexOf("/"));
                                if (titleSubfolderItemName.contains("articles")) {
                                    title.addArticle(new Article(id, fileItemName, sectionName, sectionNumber, pageNumber));
                                } else if (titleSubfolderItemName.contains("pages")) {
                                    title.addPage(new Page(id, fileItemName, sectionName, sectionNumber, pageNumber));
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("Failed on "+fileItem,e);
                            }

                        }
                    }
                }
            }

            return deliveryStatistics;
        };
    }
}
