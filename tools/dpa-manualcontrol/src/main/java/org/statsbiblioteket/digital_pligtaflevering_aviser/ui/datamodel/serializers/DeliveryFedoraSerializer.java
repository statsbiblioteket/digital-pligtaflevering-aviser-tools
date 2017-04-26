package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierachy;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/29/17.
 */
public class DeliveryFedoraSerializer {

    private HashMap<String, DomsItem> deliveryList = new HashMap<String, DomsItem>();
    private DomsParser parser = new DomsParser();
    private DomsModule domsModule = new DomsModule();
    private DomsRepository repository;

    public DeliveryFedoraSerializer(DomsRepository repository) {
        this.repository = repository;
        //eventFetch = new FetchFromDoms(repository);
    }


    public void initiateDeliveries(DeliveryFedoraSerializer.EventStatus eventStatus) {
        deliveryList.clear();
        Stream<DomsItem> items = null;

        switch(eventStatus) {
            case READYFORMANUALCHECK:
                items = getReadyForMaual();
                break;
            case DONEMANUALCHECK:
                items = getDoneManual();
                break;
        }

        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                deliveryList.put(o.getPath(), o);
            }
        });
    }


    public Stream<DomsItem> getReadyForMaual() {
        return repository.query(domsModule.providesQuerySpecification(
                "Statistics_generated", "ManualValidationDone", "", "doms:ContentModel_DPARoundTrip")
        );
    }

    public Stream<DomsItem> getDoneManual() {
        return repository.query(domsModule.providesQuerySpecification(
                "Statistics_generated,ManualValidationDone", "", "", "doms:ContentModel_DPARoundTrip")
        );
    }


    public enum EventStatus {
        READYFORMANUALCHECK, DONEMANUALCHECK;
    }




    public DomsItem getItemFromUuid(String id) {
        return repository.lookup(new DomsId(id));
    }


    public DomsItem getDeliveryFromName(String name) {
        return deliveryList.get(name);
    }

    public Set<String> getInitiatedDeliveries() {
        return deliveryList.keySet();
    }

    public void setEvent(String id, String eventName, String outcomeParameter, String message) {
        boolean outcome = outcomeParameter == null ? true : Boolean.parseBoolean(outcomeParameter);
        DomsItem item = repository.lookup(new DomsId(id));
        item.appendEvent("dashboard", new java.util.Date(), message == null ? "" : message, eventName, outcome);
    }

    /**
     * Get all Title objects from Fedora.
     *
     * @return
     * @throws Exception
     */
    public TitleDeliveryHierachy getTitleHierachyFromFedora() throws Exception {

        TitleDeliveryHierachy currentlySelectedTitleHiearachy = new TitleDeliveryHierachy();
        Iterator<String> deliveries = this.getInitiatedDeliveries().iterator();

        while(deliveries.hasNext()) {

            String delivery = deliveries.next();
            DomsItem deliveryItem = this.getDeliveryFromName(delivery);
            Iterator<DomsItem> titleSubfolder = parser.processChildDomsId().apply(deliveryItem);

            //First search through tileObjects to find all performed checks
            while(titleSubfolder.hasNext()) {

                DomsItem titleItem = titleSubfolder.next();
                Optional<DomsDatastream> validationStream = titleItem.datastreams().stream().filter(validationStreams -> validationStreams.getId().equals("VALIDATIONINFO")).findAny();

                if (validationStream.isPresent()) {

                    String validationString = validationStream.get().getDatastreamAsString();
                    StringReader reader = new StringReader(validationString);
                    InputSource inps = new InputSource(reader);
                    JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryTitleInfo.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
                    DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo)jaxbUnmarshaller.unmarshal(inps);

                    currentlySelectedTitleHiearachy.addDeliveryToTitle(deserializedObject);
                }
            }

            //Start initializing datamodel of tileObjects that has not yet been performed
            final List<DomsDatastream> datastreams = deliveryItem.datastreams();
            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getId().equals("DELIVERYSTATISTICS"))
                    .findAny();


            if (profileOptional.isPresent()) {

                DomsDatastream ds = profileOptional.get();
                //We are reading this textstring as a String and are aware that thish might leed to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inps);
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("/deliveryStatistics/titles/title/@titleName");
                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                for(int i = 0; i< nl.getLength(); i++) {
                    String titleItem = nl.item(i).getNodeValue();

                    XPathExpression articleExpression = xpath.compile("/deliveryStatistics/titles/title[@titleName='"+titleItem+"']/articles/article");
                    NodeList articles = (NodeList) articleExpression.evaluate(doc, XPathConstants.NODESET);
                    XPathExpression pagesExpression = xpath.compile("/deliveryStatistics/titles/title[@titleName='"+titleItem+"']/pages/page");
                    NodeList pages = (NodeList) pagesExpression.evaluate(doc, XPathConstants.NODESET);

                    currentlySelectedTitleHiearachy.addDeliveryToTitle(new DeliveryTitleInfo(delivery, titleItem, articles.getLength(), pages.getLength()));
                }
            }
        }
        return currentlySelectedTitleHiearachy;
    }

    /**
     * Get the object of the title in the delivery fetched from fedora
     * @param selectedDelivery
     * @param selectedTitle
     * @return
     */
    public Title getTitleObj(String selectedDelivery, String selectedTitle) {


        if(selectedDelivery==null || selectedTitle==null) {
            return null;
        }
        DomsItem domsItem = this.getDeliveryFromName(selectedDelivery);
        if(domsItem==null) {
            return null;
        }


        final List<DomsDatastream> datastreams = domsItem.datastreams();
        Optional<DomsDatastream> profileOptional = datastreams.stream()
                .filter(ds -> ds.getId().equals("DELIVERYSTATISTICS"))
                .findAny();


        if (profileOptional.isPresent()) {
            try {
                DomsDatastream ds = profileOptional.get();
                //We are reading this textstring as a String and are aware that thish might leed to encoding problems
                StringReader reader = new StringReader(ds.getDatastreamAsString());
                InputSource inps = new InputSource(reader);

                JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                DeliveryStatistics deserializedObject = (DeliveryStatistics)jaxbUnmarshaller.unmarshal(inps);


                Title selectedTitleObj = null;
                List<Title> titleList = deserializedObject.getTitles().getTitles();
                for(Title title : titleList) {
                    if(selectedTitle.equals(title.getTitle())) {
                        selectedTitleObj = title;
                    }
                }
                if(selectedTitleObj==null) {
                    return null;
                }

                return selectedTitleObj;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


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
     * @param deliveryName
     * @param titleName
     * @param statisticsStream
     */
    public boolean writeToCurrentItemInFedora(String deliveryName, String titleName, byte[] statisticsStream) {

        DomsItem domsItem = getDeliveryFromName(deliveryName);
        DomsItem selectedTitleItem = null;

        Iterator<DomsItem> titleSubfolder = parser.processChildDomsId().apply(domsItem);
        while(titleSubfolder.hasNext()) {
            DomsItem titleItem = titleSubfolder.next();
            String itemPath = titleItem.getPath();

            if(titleName.equals(itemPath.substring(itemPath.indexOf("/")+1))) {
                selectedTitleItem = titleItem;
                final List<DomsDatastream> datastreams = selectedTitleItem.datastreams();
                Optional<DomsDatastream> profileOptional = datastreams.stream()
                        .filter(ds -> ds.getId().equals("VALIDATIONINFO"))
                        .findAny();

                if (profileOptional.isPresent()) {
                    return false;
                }

                selectedTitleItem.modifyDatastreamByValue(
                        "VALIDATIONINFO",
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

}
