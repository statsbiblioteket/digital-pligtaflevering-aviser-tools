package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.TitleDeliveryHierarchy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Iterator;

/**
 * Serialize objects into files on the local filesystem.
 * This functionality can be used to optimize performance on the server by streaming the objects in a format that is used in the dayly use of the server.
 * The streaming to the filesystem is done to improve performance especially when quickly finding a title in a delivery
 */
public class DeliveryFilesystemReadWrite {

    private String cachingPath;

    public DeliveryFilesystemReadWrite(String cachingPath) {
        this.cachingPath = cachingPath;
    }

    /**
     * Ask in the filesystem if the specified month has been streamed to the filesystem
     * @param currentlySelectedMonth
     * @return
     */
    public boolean isMonthInitiated(String currentlySelectedMonth) {
        String currentFolder = cachingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        return folderForThis.exists();
    }

    /**
     * Construct the object TitleDeliveryHierarchy which contains logic about a month of deliveries
     * @param currentlySelectedMonth
     * @return the titlehierachy of all deliveries in a full month.
     * {@code null} if a delivery with this name has not been initialized from Fedora
     * @throws Exception
     */
    public TitleDeliveryHierarchy initiateTitleHierachyFromFilesystem(String currentlySelectedMonth) throws Exception {
        TitleDeliveryHierarchy currentlySelectedTitleHiearachy = new TitleDeliveryHierarchy();

        String currentFolder = cachingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if (!folderForThis.exists()) {
            return null;
        }

        File[] listOfFiles = folderForThis.listFiles();

        for (File titleFolder : listOfFiles) {
            currentlySelectedTitleHiearachy.addDeliveryToTitle(MarshallerFunctions.streamToDeliveryTitleInfo(titleFolder));
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
    public synchronized boolean saveDeliveryToFilesystem(String currentlySelectedMonth, TitleDeliveryHierarchy currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = cachingPath + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if (!folderForThis.exists()) {
            folderForThis.mkdir();
        }

        Iterator<DeliveryTitleInfo> keyIterator = currentlySelectedTitleHiearachy.getTheFullStruct().iterator();

        while (keyIterator.hasNext()) {

            DeliveryTitleInfo deliId = keyIterator.next();

            String deliName = deliId.getDeliveryName();
            String deliTitle = deliId.getNewspaperTitle();
            File fileForThisTitleDelivery = createCashingFile(currentlySelectedMonth, deliName, deliTitle);

            if (!fileForThisTitleDelivery.exists()) {
                MarshallerFunctions.streamToDeliveryTitleInfo(deliId, fileForThisTitleDelivery);
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

        File fileForThisTitleDelivery = createCashingFile(folderForThisXml, currentlySelectedTitleHiearachy.getDeliveryName(), currentlySelectedTitleHiearachy.getNewspaperTitle());

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(currentlySelectedTitleHiearachy, fileForThisTitleDelivery);

    }

    /**
     * Remove a specific cashed title in a delivery
     * @param selectedMonth
     * @param deliveryName
     * @param titleName
     * @throws Exception
     */
    public void removeDeliveryFromFilesystem(String selectedMonth, String deliveryName, String titleName) throws Exception {
        File currentFile = createCashingFile(selectedMonth, deliveryName, titleName);
        currentFile.delete();
    }

    /**
     * create cashing file on the correct location
     * @param selectedMonth
     * @param deliveryName
     * @param titleName
     * @return
     */
    public File createCashingFile(String selectedMonth, String deliveryName, String titleName) {
        File file = new File(cachingPath + selectedMonth +"/" + deliveryName + "_" + titleName + ".xml");
        return file;
    }
}
