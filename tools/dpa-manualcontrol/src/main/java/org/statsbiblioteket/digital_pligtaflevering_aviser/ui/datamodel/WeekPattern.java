package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;

@XmlRootElement
public class WeekPattern implements java.io.Serializable {

    @XmlElementWrapper
    @XmlElement
    LinkedHashMap<String, Boolean> weekDayList = new LinkedHashMap<String, Boolean>();

    public WeekPattern() {

    }

    public WeekPattern(Boolean mon, Boolean tue, Boolean wed, Boolean thu, Boolean fri, Boolean sat, Boolean sun) {
        weekDayList.put("Mon",mon);
        weekDayList.put("Tue",tue);
        weekDayList.put("Wed",wed);
        weekDayList.put("Thu",thu);
        weekDayList.put("Fri",fri);
        weekDayList.put("Sat",sat);
        weekDayList.put("Sun",sun);
    }

    public boolean getDayState(String weekDay) {
        return weekDayList.get(weekDay);
    }
}
