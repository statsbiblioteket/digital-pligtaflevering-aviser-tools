package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;

/**
 * MarshallerFunctions is a list of static functions which converts between objects that represents a newspaper
 * structure and a xmp-representation
 */
public class MarshallerFunctions {

    /**
     * Convert xml-string into a object of type DeliveryTitleInfo
     *
     * @param deliverystring
     * @return
     *
     * @throws JAXBException
     */
    public static DeliveryTitleInfo streamToDeliveryTitleInfo(String deliverystring) throws JAXBException {
        StringReader reader = new StringReader(deliverystring);
        InputSource inps = new InputSource(reader);
        JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo) jaxbUnmarshaller.unmarshal(inps);
        return deserializedObject;
    }

    /**
     * Convert xml-string into a object of type DeliveryTitleInfo
     *
     * @param serializedDeliveryfile
     * @return
     *
     * @throws JAXBException
     */
    public static DeliveryTitleInfo streamToDeliveryTitleInfo(File serializedDeliveryfile) throws JAXBException {
        JAXBContext jaxbContext1 = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext1.createUnmarshaller();
        DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo) jaxbUnmarshaller.unmarshal(serializedDeliveryfile);
        return deserializedObject;
    }

    /**
     * Stream a DeliveryTitleInfo into a file
     *
     * @param deliId
     * @param fileForThisTitleDelivery
     * @throws JAXBException
     */
    public static void streamDeliveryTitleInfoToFile(DeliveryTitleInfo deliId, File fileForThisTitleDelivery) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(DeliveryTitleInfo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(deliId, fileForThisTitleDelivery);
    }
}
