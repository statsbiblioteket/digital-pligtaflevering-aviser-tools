package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierachy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Iterator;

/**
 * Serialize objects into files on the local filesystem.
 * This functionality can be used to optimize performance on the server by streaming the objects in a format that is used in the dayly use of the server.
 */
public class DeliveryFilesystemSerializer {

    private ConfigurationMap map;
    private String cashingPath;

    public DeliveryFilesystemSerializer(String cashingPath) {
        this.cashingPath = cashingPath;
    }

    /**
     * Ask in the filesystem if the specified month has been streamed to the filesystem
     * @param currentlySelectedMonth
     * @return
     */
    public synchronized boolean isMonthInitiated(String currentlySelectedMonth) {
        String currentFolder = cashingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        return folderForThis.exists();
    }

    /**
     * Construct the object TitleDeliveryHierachy which contains logic about a month of deliveries
     * @param currentlySelectedMonth
     * @return
     * @throws Exception
     */
    public TitleDeliveryHierachy initiateTitleHierachyFromFilesystem(String currentlySelectedMonth) throws Exception {
        TitleDeliveryHierachy currentlySelectedTitleHiearachy = new TitleDeliveryHierachy();

        String currentFolder = cashingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if (!folderForThis.exists()) {
            return null;
        }

        File[] listOfFiles = folderForThis.listFiles();

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();

        for (File titleFolder : listOfFiles) {

            DeliveryTitleInfo deli = (DeliveryTitleInfo) jaxbUnMarshaller.unmarshal(titleFolder);
            currentlySelectedTitleHiearachy.addDeliveryToTitle(deli);

        }
        return currentlySelectedTitleHiearachy;
    }


    /**
     * Write all titles in the delivery to the filesystem
     * @param currentlySelectedMonth
     * @param currentlySelectedTitleHiearachy
     * @return
     * @throws Exception
     */
    public synchronized boolean saveDeliveryToFilesystem(String currentlySelectedMonth, TitleDeliveryHierachy currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = cashingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if (!folderForThis.exists()) {
            folderForThis.mkdir();
        }

        Iterator<DeliveryTitleInfo> keyIterator = currentlySelectedTitleHiearachy.getTheFullStruct().iterator();

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


        while (keyIterator.hasNext()) {

            DeliveryTitleInfo deliId = keyIterator.next();

            String deliName = deliId.getDeliveryName();
            String deliTitle = deliId.getNewspaperTitle();
            File fileForThisTitleDelivery = new File(currentFolder + "/" + deliName + "_" + deliTitle + ".xml");

            if (!fileForThisTitleDelivery.exists()) {
                jaxbMarshaller.marshal(deliId, fileForThisTitleDelivery);
            }
        }
        return true;
    }

    /**
     * Write the specific title in the delivery to the filesystem
     * @param folderForThisXml
     * @param currentlySelectedTitleHiearachy
     * @throws Exception
     */
    public void saveDeliveryToFilesystem(String folderForThisXml, DeliveryTitleInfo currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = cashingPath + folderForThisXml +"/" + currentlySelectedTitleHiearachy.getDeliveryName() + "_" + currentlySelectedTitleHiearachy.getNewspaperTitle() + ".xml";

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(currentlySelectedTitleHiearachy, new File(currentFolder));

    }

    /**
     * Remove a specific cashed title in a delivery
     * @param folderForThisXml
     * @param deliveryName
     * @param titleName
     * @throws Exception
     */
    public void removeDeliveryFromFilesystem(String folderForThisXml, String deliveryName, String titleName) throws Exception {
        File currentFile = new File(cashingPath + folderForThisXml +"/" + deliveryName + "_" + titleName + ".xml");
        currentFile.delete();
    }
}
