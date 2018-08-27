package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.util.function.Function;

public class JaxbUtils {
    
    public final static JAXBContext jaxbContext;
    
    static {
        try { //JaxbContext is threadsafe and expensive. https://stackoverflow.com/a/7400735/4527948
            jaxbContext = JAXBContext.newInstance(DeliveryStatistics.class, DeliveryPattern.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to create JAXBContext",e);
        }
    }
    
    
    /**
     * convert DeliveryStatistics into a bytseArray which can be saved in doms
     * @return
     */
    public static Function<DeliveryStatistics, byte[]> processDeliveryStatisticsToBytestream() {
        return deliveryStatistics -> {
            try (ByteArrayOutputStream deliveryArrayStream = new ByteArrayOutputStream()){
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(deliveryStatistics, deliveryArrayStream);
                return deliveryArrayStream.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Failed to transform DeliveryStatistics to xml",e);
            }
        };
    }
}
