package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;


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

    public Stream<Map.Entry<String, WeekPattern>> entryStream() {
        return weekPatterns.entrySet().stream();
    }
}
