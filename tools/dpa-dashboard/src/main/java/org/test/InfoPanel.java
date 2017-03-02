package org.test;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;

import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class InfoPanel extends Table {
    String[] stateList = {"Manually_stopped", "Data_Created", "Data_Archived"};

    public InfoPanel() {
        super("States");
        this.addContainerProperty("State", String.class, null);
        this.addContainerProperty("amount", Long.class, null);


        this.setWidth("50%");
    }

    public void readStates(FetchEventStructure fetchStructure) {
        for(String state : stateList) {
            Object newItemId = this.addItem();
            com.vaadin.data.Item row1 = this.getItem(newItemId);

            row1.getItemProperty("State").setValue(state);
            row1.getItemProperty("amount").setValue(new Long(fetchStructure.getState(state).count()));

        }
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        super.addItemClickListener(listener);
    }




}
