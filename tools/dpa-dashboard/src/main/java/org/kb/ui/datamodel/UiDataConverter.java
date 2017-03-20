package org.kb.ui.datamodel;


import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.junit.Test;

import javax.swing.text.DateFormatter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mmj on 3/9/17.
 */
public class UiDataConverter {

    private static String dateFormat = "yyyyMMdd";
    private static DateFormatter df = new DateFormatter();

    public static ArrayList<FileComponent> getComponent(DeliveryStatistics delivery) {

        ArrayList<FileComponent> list = new ArrayList<FileComponent>();
        for(Title title : delivery.getTitles().getTitles()) {
            Iterator<Page> pageIterator = title.getPage().iterator();
            while(pageIterator.hasNext()) {
                Page page = pageIterator.next();
                FileComponent c = new FileComponent(page.getPageName(), title.getTitle(), "Undefined");
                list.add(c);
            }
        }



        return list;
    }


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


}
