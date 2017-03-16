package org.kb.ui.datamodel;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mmj on 3/16/17.
 */
public class DataformatConverter {

    private HashMap<String, ArrayList<Title>> titleFilterOnTitlename = new HashMap<String, ArrayList<Title>>();


    public void setIt(DeliveryStatistics stat) {

        for(Title title : stat.getTitles().getTitles()) {

            ArrayList<Title> li;
            if(titleFilterOnTitlename.containsKey(title.getTitle())) {
                li = titleFilterOnTitlename.get(title.getTitle());
            } else {
                li = new ArrayList<Title>();
                titleFilterOnTitlename.put(title.getTitle(), li);
            }

            li.add(title);


        }

    }



    public ArrayList<Title> getIt(String filter) {

        return titleFilterOnTitlename.get(filter);

    }



}
