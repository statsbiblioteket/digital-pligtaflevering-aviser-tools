package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;


@XmlRootElement
public class DeliveryPattern implements java.io.Serializable {

    @XmlElementWrapper
    @XmlElement
    LinkedHashMap<String, WeekPattern> weekPatterns = new LinkedHashMap<String, WeekPattern>();

    public void addDeliveryPattern(String name, WeekPattern weekPattern) {
        this.weekPatterns.put(name, weekPattern);
    }

    public WeekPattern getWeekPattern(String name) {
        return this.weekPatterns.get(name);
    }
}
