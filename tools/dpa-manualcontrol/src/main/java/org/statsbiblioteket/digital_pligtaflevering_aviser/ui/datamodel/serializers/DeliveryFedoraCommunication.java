package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierarchy;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper-class for serializing objects between datamodel and fedora This is used for writing xml-streams to fedora and
 * reading xml-streams into objectmodels. Each roundtrip contains a datastream with metadata of the filestructure under
 * each delivery. The result af a check is stored in the Title under a delivery
 */
public class DeliveryFedoraCommunication {
    protected Logger log = LoggerFactory.getLogger(getClass());

    private static final String VALIDATION_INFO_STREAMNAME = "VALIDATIONINFO";
    private static final String STATISTICS_STREAM_NAME = "DELIVERYSTATISTICS";

    private static String itemType;
    private static String pastEvents;
    private static String thisEvent;

    private HashMap<String, DomsItem> deliveryList = new HashMap<>();
    private DomsParser parser = new DomsParser();
    private DomsModule domsModule = new DomsModule();
    private DomsRepository repository;

    public DeliveryFedoraCommunication(String itemType, String pastEvents, String thisEvent, DomsRepository repository) {
        this.itemType = itemType;
        this.pastEvents = pastEvents;
        this.thisEvent = thisEvent;
        this.repository = repository;
    }

    /**
     * Initiate the list of deliveries
     *
     * @param eventStatus
     * @param deliveryFilter
     */
    public void initiateDeliveries(DeliveryFedoraCommunication.EventStatus eventStatus, String deliveryFilter) {
        deliveryList.clear();
        Stream<DomsItem> items = null;

        switch (eventStatus) {
            case READYFORMANUALCHECK:
                items = getReadyForManual(deliveryFilter);
                break;
            case DONEMANUALCHECK:
                items = getDoneManual(deliveryFilter);
                break;
            case CREATEDONLY:
                items = getCreatedOnly(deliveryFilter);
                break;

        }

        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                deliveryList.put(o.getPath(), o);
            }
        });
    }

    /**
     * Get a list of deliveries, which are ready for manual inspections
     *
     * @param deliveryFilter
     * @return
     */
    public Stream<DomsItem> getReadyForManual(String deliveryFilter) {
        return repository.query(domsModule.providesWorkToDoQuerySpecification(
                pastEvents, thisEvent, "", itemType))
                .filter(ts -> ts.getPath().contains(deliveryFilter));
    }


    public Stream<DomsItem> getCreatedOnly(String deliveryFilter) {
        return repository.query(domsModule.providesWorkToDoQuerySpecification(
                "Data_Received", "", "", itemType))
                .filter(ts -> ts.getPath().contains(deliveryFilter));
    }


    /**
     * Get a list of deliveries, which is has already added an event that manual inspections has been done
     *
     * @param deliveryFilter
     * @return
     */
    public Stream<DomsItem> getDoneManual(String deliveryFilter) {
        return repository.query(domsModule.providesWorkToDoQuerySpecification(
                pastEvents, "", "", itemType))
                .filter(ts -> ts.getPath().contains(deliveryFilter));
    }

    /**
     * Get the domsItem from the uuid. The Item is fetched directly from fedora
     *
     * @param id
     * @return
     */
    public DomsItem getItemFromUuid(String id) {
        return repository.lookup(new DomsId(id));
    }

    /**
     * Get the DomsItem from the list of items, which has been cashed
     *
     * @param name deliveryname as used in fedora "dl_######_rt#"
     * @return the DomsItem which is used for iterating through the tree-structure below the item, {@code null} if a
     * delivery with this name has not been initialized from Fedora
     */
    public DomsItem getDeliveryFromName(String name) {
        return deliveryList.get(name);
    }

    /**
     * Get the list of deliveryNames which are cashed
     *
     * @return
     */
    public Set<String> getInitiatedDeliveries() {
        return deliveryList.keySet();
    }

    /**
     * Get all Title objects from Fedora.
     *
     * @return
     *
     * @throws Exception
     */
    public TitleDeliveryHierarchy getTitleHierachyFromFedora() throws Exception {

        TitleDeliveryHierarchy currentlySelectedTitleHiearachy = new TitleDeliveryHierarchy();

        for (String delivery : deliveryList.keySet()) {

            DomsItem deliveryItem = deliveryList.get(delivery);
            Iterator<DomsItem> titleSubfolder = parser.processChildDomsId().apply(deliveryItem);

            //First search through tileObjects to find all performed checks
            while (titleSubfolder.hasNext()) {

                DomsItem titleItem = titleSubfolder.next();
                Optional<DomsDatastream> validationStream = titleItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals(VALIDATION_INFO_STREAMNAME)).findAny();

                if (validationStream.isPresent()) {
                    String validationString = validationStream.get().getDatastreamAsString();
                    currentlySelectedTitleHiearachy.addDeliveryToTitle(MarshallerFunctions.streamToDeliveryTitleInfo(validationString));
                }
            }

            //Start initializing datamodel of tileObjects that has not yet been performed
            final List<DomsDatastream> datastreams = deliveryItem.datastreams();
            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getId().equals(STATISTICS_STREAM_NAME))
                    .findAny();

            //<deliveryStatistics deliveryName="dl_20160811_rt1"><titles><title titleName="verapdf"><pages><page checkedState="UNCHECKED" id="uuid:f1642d44-fe50-441e-bedb-99562a034353" pageName="dl_20160811_rt1/verapdf/pages/20160811_verapdf_section01_page005_v20160811x1#0005" pageNumber="1" sectionName="undefined" sectionNumber="1"/>
            if (profileOptional.isPresent()) {

                DomsDatastream ds = profileOptional.get();
                //We are reading this textstring as a String and are aware that this might lead to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inps);
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("/deliveryStatistics/*[local-name()='titles']/*[local-name()='title']/@titleName");
                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                for (int i = 0; i < nl.getLength(); i++) {
                    String titleItem = nl.item(i).getNodeValue();

                    XPathExpression articleExpression = xpath.compile("/deliveryStatistics/*[local-name()='titles']/*[local-name()='title'][@titleName='" + titleItem + "']/*[local-name()='articles']/*[local-name()='article']");
                    NodeList articles = (NodeList) articleExpression.evaluate(doc, XPathConstants.NODESET);
                    XPathExpression pagesExpression = xpath.compile("/deliveryStatistics/*[local-name()='titles']/*[local-name()='title'][@titleName='" + titleItem + "']/*[local-name()='pages']/*[local-name()='page']");
                    NodeList pages = (NodeList) pagesExpression.evaluate(doc, XPathConstants.NODESET);

                    currentlySelectedTitleHiearachy.addDeliveryToTitle(new DeliveryTitleInfo(delivery, titleItem, articles.getLength(), pages.getLength()));
                }
            }
        }
        return currentlySelectedTitleHiearachy;
    }

    /**
     * Get the object of the title in the delivery fetched from fedora
     *
     * @param selectedDelivery
     * @param selectedTitle
     * @return the Title which contains metadate of a newspapertitle in a delivery {@code null} if a delivery with this
     * name has not been initialized from Fedora
     */
    public Title getTitleObj(String selectedDelivery, String selectedTitle) {
        if (selectedDelivery == null || selectedTitle == null) {
            return null;
        }
        DomsItem domsItem = this.getDeliveryFromName(selectedDelivery);
        if (domsItem == null) {
            return null;
        }

        final List<DomsDatastream> datastreams = domsItem.datastreams();
        Optional<DomsDatastream> profileOptional = datastreams.stream()
                .filter(ds -> ds.getId().equals(STATISTICS_STREAM_NAME))
                .findAny();

        if (profileOptional.isPresent()) {
            try {
                DomsDatastream ds = profileOptional.get();
                //We are reading this textstring as a String and are aware that this might leed to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                DeliveryStatistics deserializedObject = (DeliveryStatistics) jaxbUnmarshaller.unmarshal(inps);

                Title selectedTitleObj = null;
                List<Title> titleList = deserializedObject.getTitles().getTitles();
                for (Title title : titleList) {
                    if (selectedTitle.equals(title.getTitleName())) {
                        selectedTitleObj = title;
                    }
                }
                if (selectedTitleObj == null) {
                    return null;
                }

                return selectedTitleObj;

            } catch (JAXBException e) {
                log.error("Parsing of XML-streams has failed", e);
                return null;
            }
        }
        return null;
    }

    /**
     * Write the delivery to the title-object in fedora
     *
     * @param deli
     * @return
     *
     * @throws JAXBException
     */
    public boolean writeDeliveryToFedora(DeliveryTitleInfo deli) throws JAXBException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(deli, os);
        return writeToCurrentItemInFedora(deli.getDeliveryName(), deli.getNewspaperTitle(), os.toByteArray());
    }

    /**
     * Write to current title in delivery
     *
     * @param deliveryName
     * @param titleName
     * @param statisticsStream
     * @return return true if the writing performed successfully
     */
    public boolean writeToCurrentItemInFedora(String deliveryName, String titleName, byte[] statisticsStream) {

        DomsItem domsItem = getDeliveryFromName(deliveryName);
        if (domsItem == null) {
            return false;
        }
        DomsItem selectedTitleItem;

        Iterator<DomsItem> titleSubfolder = parser.processChildDomsId().apply(domsItem);
        while (titleSubfolder.hasNext()) {
            DomsItem titleItem = titleSubfolder.next();
            String itemPath = titleItem.getPath();

            //path:dl_20160811_rt1/verapdf
            //Find the title part of the path, the title part is the title of the newspaper (bt/politikken/jyllandsposten etc.)
            if (titleName.equals(itemPath.substring(itemPath.indexOf("/") + 1))) {
                selectedTitleItem = titleItem;
                final List<DomsDatastream> datastreams = selectedTitleItem.datastreams();
                Optional<DomsDatastream> profileOptional = datastreams.stream()
                        .filter(ds -> ds.getId().equals(VALIDATION_INFO_STREAMNAME))
                        .findAny();

                if (profileOptional.isPresent()) {
                    return false;
                }

                selectedTitleItem.modifyDatastreamByValue(
                        VALIDATION_INFO_STREAMNAME,
                        null, // no checksum
                        null, // no checksum
                        statisticsStream,
                        null,
                        "text/xml",
                        null,
                        new java.util.Date().getTime());
                return true;
            }
        }
        return false;
    }

    /**
     * Enums for different types of fetch to fedora
     */
    public enum EventStatus {
        /**
         * Get deliveries that is ready for manual check
         */
        READYFORMANUALCHECK,

        /**
         * Get deliveries including deliveries where the manual check is done
         */
        DONEMANUALCHECK,

        CREATEDONLY;
    }
}
