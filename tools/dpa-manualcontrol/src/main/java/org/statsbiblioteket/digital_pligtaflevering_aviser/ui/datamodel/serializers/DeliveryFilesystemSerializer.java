package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

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


    public synchronized boolean saveCurrentTitleHierachyToFilesystem(String currentlySelectedMonth, TitleDeliveryHierachy currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = "/home/mmj/tools/tomcat/" + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if (!folderForThis.exists()) {
            folderForThis.mkdir();
        }
        else {
            return false;
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
            File folderForThisTitleDelivery = new File(currentFolder + "/" + deliName + "_" + deliTitle + ".xml");

            if (!folderForThisTitleDelivery.exists()) {
                jaxbMarshaller.marshal(deliId, folderForThisTitleDelivery);
            }
        }
        return true;
    }

    public synchronized boolean isMonthInitiated(String currentlySelectedMonth) {

        String currentFolder = "/home/mmj/tools/tomcat/" + currentlySelectedMonth;

        File folderForThis = new File(currentFolder);
        return folderForThis.exists();
    }


    public TitleDeliveryHierachy initiateTitleHierachyFromFilesystem(String currentlySelectedMonth) throws Exception {
        TitleDeliveryHierachy currentlySelectedTitleHiearachy = new TitleDeliveryHierachy();

        String currentFolder = "/home/mmj/tools/tomcat/" + currentlySelectedMonth;
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


    public void saveCurrentTitleHierachyToFilesystem(String folderForThisXml, DeliveryIdentifier currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = "/home/mmj/tools/tomcat/" + folderForThisXml +"/" + currentlySelectedTitleHiearachy.getDeliveryName() + "_" + currentlySelectedTitleHiearachy.getNewspaperTitle() + ".xml";

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(currentlySelectedTitleHiearachy, new File(currentFolder));

    }

}
