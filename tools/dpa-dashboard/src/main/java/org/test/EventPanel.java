package org.test;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;

import java.util.Date;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class EventPanel extends Table {
    String[] stateList = {"Event", "sucess", "Date"};

    public EventPanel() {
        super("Events");
        this.addContainerProperty("Batch", String.class, null);
        this.addContainerProperty("sucess", Boolean.class, null);
        this.addContainerProperty("Date", Date.class, null);



        this.setWidth("100%");
    }

    public void setInfo(FetchEventStructure fetchStructure, String info) {

        super.setCaption(info);


        Stream<DomsItem> items = fetchStructure.getState(info);



        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                Object newItemId = addItem();
                com.vaadin.data.Item row1 = getItem(newItemId);
                if(info.equals(o.getPath())) {

                }
            }

        });



    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        super.addItemClickListener(listener);
    }




}
