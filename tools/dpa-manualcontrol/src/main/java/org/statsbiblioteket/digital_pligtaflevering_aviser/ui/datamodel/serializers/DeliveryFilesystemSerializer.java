package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierachy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Iterator;

/**
 * Created by mmj on 3/29/17.
 */
public class DeliveryFilesystemSerializer {

    private ConfigurationMap map;
    private String cashingPath;

    public DeliveryFilesystemSerializer(String cashingPath) {
        this.cashingPath = cashingPath;
    }

    public synchronized boolean isMonthInitiated(String currentlySelectedMonth) {

        String currentFolder = cashingPath + currentlySelectedMonth;

        File folderForThis = new File(currentFolder);
        return folderForThis.exists();
    }


    public TitleDeliveryHierachy initiateTitleHierachyFromFilesystem(String currentlySelectedMonth) throws Exception {
        TitleDeliveryHierachy currentlySelectedTitleHiearachy = new TitleDeliveryHierachy();

        String currentFolder = cashingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if (!folderForThis.exists()) {
            return null;
        }

        File[] listOfFiles = folderForThis.listFiles();

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
        Unmarshaller jaxbUnMarshaller = jaxbContext.createUnmarshaller();

        for (File titleFolder : listOfFiles) {

            DeliveryIdentifier deli = (DeliveryIdentifier) jaxbUnMarshaller.unmarshal(titleFolder);
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

        Iterator<DeliveryIdentifier> keyIterator = currentlySelectedTitleHiearachy.getTheFullStruct().iterator();

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


        while (keyIterator.hasNext()) {

            DeliveryIdentifier deliId = keyIterator.next();

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
    public void saveDeliveryToFilesystem(String folderForThisXml, DeliveryIdentifier currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = cashingPath + folderForThisXml +"/" + currentlySelectedTitleHiearachy.getDeliveryName() + "_" + currentlySelectedTitleHiearachy.getNewspaperTitle() + ".xml";

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
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
