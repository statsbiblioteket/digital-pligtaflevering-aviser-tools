package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.serializers;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DeliveryPattern;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.JaxbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * MarshallerFunctions is a list of static functions which converts between objects that represents a newspaper
 * structure and a xmp-representation
 */
public class MarshallerFunctions {
    
    protected static Logger log = LoggerFactory.getLogger(MarshallerFunctions.class);
    
    
    
    
    public final static JAXBContext jaxbContext;
    
    static {
        try { //JaxbContext is threadsafe and expensive. https://stackoverflow.com/a/7400735/4527948
            jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class, DeliveryPattern.class, DeliveryTitleInfo.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to create JAXBContext",e);
        }
    }
    
    /**
     * Convert xml-string into a object of type DeliveryTitleInfo
     *
     * @param deliverystring
     * @return
     *
     * @throws JAXBException
     */
    public static DeliveryTitleInfo toDeliveryTitleInfo(String deliverystring) throws JAXBException {
        StringReader reader = new StringReader(deliverystring);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        DeliveryTitleInfo deserializedObject = (DeliveryTitleInfo) jaxbUnmarshaller.unmarshal(reader);
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
    public static DeliveryTitleInfo toDeliveryTitleInfo(File serializedDeliveryfile) throws JAXBException {
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
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

        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(deliId, fileForThisTitleDelivery);
    }
    
    public static DeliveryPattern unmarshallDeliveryPattern(File configFile) {
        DeliveryPattern deliveryPattern1 = null;
        if (configFile.exists()) {
            try (InputStream is = new FileInputStream(configFile);) {
                Unmarshaller jaxbUnmarshaller = JaxbUtils.jaxbContext.createUnmarshaller();
                deliveryPattern1 = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);
            } catch (JAXBException | IOException e) {
                log.error(e.getMessage());
            }
        }
        return deliveryPattern1;
    }
    
    public static ByteArrayOutputStream marshallDeliveryTitleInfo(DeliveryTitleInfo deli) throws JAXBException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(deli, os);
        return os;
    }
}
