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
    private static DateFormatter df = new DateFormatter();


    /**
     * Return a date from a deliveryname
     * @param deliveryItemDirectoryName
     * @return
     * @throws ParseException
     */
    public static Date getDateFromDeliveryItemDirectoryName(String deliveryItemDirectoryName) throws ParseException {

        Pattern pattern = Pattern.compile("dl_(.*)_rt([0-9]+)$");
        Matcher matcher = pattern.matcher(deliveryItemDirectoryName);
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
     * Convert a pageiterator into a hashMap of pages, and use filter if delivered
     * @param pageList
     * @return
     */
    public static Map sectionConverter(List<Page> pageList) {

        Map<String, List<Page>> grouped = pageList.stream().collect(Collectors.groupingBy(pageItem -> pageItem.getSectionNumber()));
        HashMap<String, TitleComponent> hset = new HashMap<String, TitleComponent>();
        for(List<Page> groupedPageList : grouped.values()) {
            hset.put(groupedPageList.get(0).getSectionNumber(), new TitleComponent(groupedPageList.get(0).getSectionName(), groupedPageList.get(0).getSectionNumber(), groupedPageList.size()));
        }
        return hset;
    }
}
