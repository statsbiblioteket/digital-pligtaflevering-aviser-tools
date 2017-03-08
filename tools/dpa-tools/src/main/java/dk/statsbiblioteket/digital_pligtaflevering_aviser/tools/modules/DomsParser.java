package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Article;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
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

            String deliveryName = domsItem.getPath();
            Stream<DomsItem> ss =domsItem.children();
            Iterator<DomsItem> it = ss.iterator();

            return it;
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

            while(roundtripIterator.hasNext()) {

                DomsItem titleItem = roundtripIterator.next();
                Iterator<DomsItem> titleSubfolder = this.processChildDomsId().apply(titleItem);
                Title title = new Title(titleItem.getPath());
                deliveryStatistics.addTitle(title);

                while(titleSubfolder.hasNext()) {
                    DomsItem titleSubfolderItem = titleSubfolder.next();
                    Stream<DomsItem> fileStream =titleSubfolderItem.children();
                    Iterator<DomsItem> fileItemIterator = fileStream.iterator();
                    while(fileItemIterator.hasNext()) {
                        DomsItem fileItem = fileItemIterator.next();
                        String titleSubfolderItemName = titleSubfolderItem.getPath();
                        if(titleSubfolderItemName.contains("articles")) {
                            title.addArticle(new Article(fileItem.getPath()));
                        } else if(titleSubfolderItemName.contains("pages")) {
                            title.addPage(new Page(fileItem.getPath()));
                        }
                    }
                }
            }

            return deliveryStatistics;
        };
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
