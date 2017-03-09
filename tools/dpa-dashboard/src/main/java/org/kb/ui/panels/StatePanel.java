package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.kb.ui.FetchEventStructure;

import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class StatePanel extends Table {
    String[] stateList = {"Manually_stopped", "Data_Created", "Data_Archived"};

    public StatePanel() {
        super("States");
        this.addContainerProperty("State", String.class, null);
        this.addContainerProperty("amount", Long.class, null);
        //this.addContainerProperty("DomsItem", Stream.class, null);
        this.setWidth("100%");
    }

    public void readStates(FetchEventStructure fetchStructure) {
        for(String state : stateList) {
            Object newItemId = this.addItem();
            com.vaadin.data.Item row1 = this.getItem(newItemId);
            Stream<DomsItem> item = fetchStructure.getState(state);
            row1.getItemProperty("State").setValue(state);
            row1.getItemProperty("amount").setValue(new Long(item.count()));
            //row1.getItemProperty("DomsItem").setValue(item);
        }
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        super.addItemClickListener(listener);
    }




}
