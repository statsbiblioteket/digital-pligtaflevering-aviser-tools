package org.kb.ui.datamodel.serializers;

import org.kb.ui.datamodel.DeliveryIdentifier;
import org.kb.ui.datamodel.DeliveryIdentifiers;
import org.kb.ui.datamodel.TitleDeliveryHierachy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.Iterator;

/**
 * Created by mmj on 3/29/17.
 */
public class DeliverySerializer {


    public static void saveCurrentTitleHierachyToFilesystem(String currentlySelectedMonth, TitleDeliveryHierachy currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = "/home/mmj/tools/tomcat/" + currentlySelectedMonth;
        File folderForThis = new File(currentFolder);
        if(!folderForThis.exists()) {
            folderForThis.mkdir();
        }

        Iterator<String> keyIterator = currentlySelectedTitleHiearachy.getDeliveryStructure().keySet().iterator();

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


        while(keyIterator.hasNext()) {

            String currentKey = keyIterator.next();
            String currentTitleDeliveryFile = currentFolder + "/" + currentKey;
            File folderForThisTitle = new File(currentTitleDeliveryFile);
            if(!folderForThisTitle.exists()) {
                folderForThisTitle.mkdir();
            }

            DeliveryIdentifiers deli = currentlySelectedTitleHiearachy.getDeliveryStructure().get(currentKey);

            for(DeliveryIdentifier deliId : deli.getDeliveries()) {

                String deliName = deliId.getName();
                File folderForThisTitleDelivery = new File(currentTitleDeliveryFile + "/" + deliName + ".xml");

                if(!folderForThisTitleDelivery.exists()) {
                    jaxbMarshaller.marshal(deliId, folderForThisTitleDelivery);
                }
            }
        }
    }



    public static void saveCurrentTitleHierachyToFilesystem(String folderForThisXml, DeliveryIdentifier currentlySelectedTitleHiearachy) throws Exception {

        String currentFolder = "/home/mmj/tools/tomcat/" + folderForThisXml + ".xml";

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryIdentifier.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(currentlySelectedTitleHiearachy, new File(currentFolder));

    }

}
