package org.kb.ui.tableBeans;


import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mmj on 3/9/17.
 */
public class UiDataConverter {

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


}
