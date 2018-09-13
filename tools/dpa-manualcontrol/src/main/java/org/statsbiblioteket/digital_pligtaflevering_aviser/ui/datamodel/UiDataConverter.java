package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryInformationComponent.ValidationState;

import javax.swing.text.DateFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Converter utilities between deliveryDTO's and DTO's suitable for the UI
 */
public class UiDataConverter {

    private static String dateFormat = "yyyyMMdd";
    private static Pattern deliveryPattern = Pattern.compile("dl_(.*)_rt([0-9]+)$");
    private static DateFormatter df = new DateFormatter();


    /**
     * Return a date from a deliveryname
     * @param deliveryItemDirectoryName
     * @return
     * @throws ParseException
     */
    public static synchronized Date getDateFromDeliveryItemDirectoryName(String deliveryItemDirectoryName) throws ParseException {

        Matcher matcher = deliveryPattern.matcher(deliveryItemDirectoryName);
        if (matcher.matches()) {
            String datePart = matcher.group(1);
            df.setFormat(new SimpleDateFormat(dateFormat));
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            Date formattedDate = formatter.parse(datePart);
            return formattedDate;

        } else {
            return null;
        }
    }

    /**
     * Return a datematcher from the pattern defined by deliveries from Infomedia
     * @param deliveryItemDirectoryName
     * @return
     * @throws ParseException
     */
    public static synchronized Matcher getPatternMatcher(String deliveryItemDirectoryName) throws ParseException {
        Matcher matcher = deliveryPattern.matcher(deliveryItemDirectoryName);
        return matcher;
    }


    /**
     * Convert a list of pages into a map of TitleComponent, a new TitleComponent is added for every different page found in the list
     * @param pageList
     * @return
     */
    public static synchronized Map sectionConverter(List<Page> pageList) {

        Map<String, List<Page>> grouped = pageList.stream().collect(Collectors.groupingBy(pageItem -> pageItem.getSectionNumber()));
        Map<Integer, TitleComponent> titleComponentList = new HashMap<Integer, TitleComponent>();
        for (List<Page> groupedPageList : grouped.values()) {
            Page firstPage = groupedPageList.get(0);
            titleComponentList.put(Integer.parseInt(firstPage.getSectionNumber()), new TitleComponent(firstPage.getSectionName(), Integer.parseInt(firstPage.getSectionNumber()), groupedPageList.size()));
        }
        return titleComponentList;
    }


    /**
     * Iterate through all Events and return SUCCES if they have all been validated
     * @param events
     * @return
     */
    public static ValidationState validateEventCollection(List<Event> events) {
        //Only get the latest event for each ID
        Map<String, Event> eventMap = events.stream()
                                            .collect(Collectors.toMap(
                                                    event -> event.getEventID(), //keymapper
                                                    event -> event, //valuemapper
                                                    (event1, event2) -> { //merge function if the keys are equal
                                                        if (event1.getDate()
                                                                  .compareTo(event2.getDate()) >= 0) {
                                                            return event1;
                                                        } else {
                                                            return event2;
                                                        }
                                                    }));
        
        if (eventMap.get(Constants.APPROVED_EVENT) != null){
            return ValidationState.APPROVED;
        }
        if (eventMap.get(Constants.MANUAL_QA_COMPLETE_EVENT) != null){
            return ValidationState.MANUAL_QA_COMPLETE;
        }
        if (eventMap.get(Constants.STOPPED_EVENT) != null){
            return ValidationState.STOPPED;
        }
        if (eventMap.values().stream().anyMatch(event -> !event.isSuccess())){
            return ValidationState.FAIL;
        } else {
            return ValidationState.PROGRESS;
        }
    }
    
    public static boolean isEventOverridden(List<Event> originalEvents) {
        return originalEvents.stream().anyMatch(event -> Constants.OVERRIDE_EVENT.equals(event.getEventID()));
    }
    
    public static List<EventDTO> convertList(List<Event> events) {
        List<EventDTO> returnEventList = new ArrayList<EventDTO>();
        for(Event event : events) {
            returnEventList.add(new EventDTO(event));
        }
        return returnEventList;
    }
}
