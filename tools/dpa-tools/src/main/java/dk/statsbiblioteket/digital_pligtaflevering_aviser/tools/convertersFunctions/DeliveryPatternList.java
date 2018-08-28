package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeliveryPatternList {
    protected static final Logger log = LoggerFactory.getLogger(DeliveryPatternList.class);
    
    //Linked hashmap to preserve insertion order
    private LinkedHashMap<String,DeliveryPattern> patterns = new LinkedHashMap<>();

    public DeliveryPattern getMostRecentDeliveryPatternBefore(String deliveryName){
    
        //LinkedHashMap so we iterate in insertion order
        for (Map.Entry<String, DeliveryPattern> entry : patterns.entrySet()) {
            log.debug("Comparing delivery {} to entry {}",deliveryName,entry.getKey());
            if (entry.getKey().compareTo(deliveryName) >= 0){
                log.debug("Found deliveryPattern with stop-date {} after delivery date {}",entry.getKey(),deliveryName);
                return entry.getValue();
            }
        }
        log.warn("Found no delivery pattern for delivery {}, returning null",deliveryName);
        return null;
    }
    
    protected DeliveryPatternList add(String until,DeliveryPattern pattern){
        patterns.put(until,pattern);
        return this;
    }
    
    
    public static DeliveryPatternList parseFromString(String deliveryPatterns){
        log.trace("Parsing patterns {}",deliveryPatterns);
        Unmarshaller jaxbUnmarshaller;
        try {
            jaxbUnmarshaller = JaxbUtils.jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to create jaxb unmarshaller",e);
        }
    
        DeliveryPatternList patternList = new DeliveryPatternList();
        String[] patterns = deliveryPatterns.split(",");
        for (String pattern : patterns) {
            log.debug("Found pattern {}",pattern);
            String[] nameAndEnd = pattern.split(":", 2);
        
            DeliveryPattern deliveryPattern;
            String xml_path = nameAndEnd[0].trim();
            try (InputStream is = new FileInputStream(xml_path);) {
                deliveryPattern = (DeliveryPattern) jaxbUnmarshaller.unmarshal(is);
            } catch (Exception e){
                throw new RuntimeException("Failed to load deliveryPattern from '" + pattern + "'",e);
            }
        
            if (nameAndEnd.length == 1){
                //There was no stop date, so set the max deliveryname as stop date
                String stop_date = "dl_99999999_rt9";
                patternList.add(stop_date, deliveryPattern);
                log.info("Pattern is without a stop date, so setting stop date={} and ignoring further patterns",stop_date);
                //As this will match all deliveries, that have not been matched by a previous pattern, stop parsing further pattern entries
                break;
            } else {
                String stop_date = nameAndEnd[1].trim();
                log.debug("Adding ('{}','{}') to deliveryPatterns", stop_date, xml_path);
                patternList.add(stop_date, deliveryPattern);
            }
        }
        return patternList;
    }
    
    @Override
    public String toString() {
        return "DeliveryPatternList{" +
               "patterns=" + patterns +
               '}';
    }
}
