package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.DayOfWeek;
import java.util.LinkedHashMap;

@XmlRootElement
public class WeekPattern implements java.io.Serializable {

    @XmlElementWrapper
    @XmlElement
    LinkedHashMap<DayOfWeek, Boolean> weekDayList = new LinkedHashMap<>();

    public WeekPattern() {

    }

    public WeekPattern(Boolean mon, Boolean tue, Boolean wed, Boolean thu, Boolean fri, Boolean sat, Boolean sun) {
        weekDayList.put(DayOfWeek.MONDAY,mon);
        weekDayList.put(DayOfWeek.TUESDAY,tue);
        weekDayList.put(DayOfWeek.WEDNESDAY,wed);
        weekDayList.put(DayOfWeek.THURSDAY,thu);
        weekDayList.put(DayOfWeek.FRIDAY,fri);
        weekDayList.put(DayOfWeek.SATURDAY,sat);
        weekDayList.put(DayOfWeek.SUNDAY,sun);
    }

    public boolean getDayState(DayOfWeek weekDay) {
        return weekDayList.get(weekDay);
    }
}
