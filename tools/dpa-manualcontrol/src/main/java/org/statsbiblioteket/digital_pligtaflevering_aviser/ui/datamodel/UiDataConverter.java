package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;

import javax.swing.text.DateFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        Map<String, TitleComponent> titleComponentList = new HashMap<String, TitleComponent>();
        for (List<Page> groupedPageList : grouped.values()) {
            Page firstPage = groupedPageList.get(0);
            titleComponentList.put(firstPage.getSectionNumber(), new TitleComponent(firstPage.getSectionName(), firstPage.getSectionNumber(), groupedPageList.size()));
        }
        return titleComponentList;
    }
}
