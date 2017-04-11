package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;

import java.util.Collection;
import java.util.HashMap;

/**
 * List containing single String items
 */
public class SingleStringListPanel extends VerticalLayout {

    private Table table;

    public SingleStringListPanel() {

        // Bind a table to it
        table = new Table("List");
        table.addContainerProperty("Item", String.class, null);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(table);
    }


    public void setTableContent(Collection<String> list) {
        table.removeAllItems();
        for(String item : list) {
            Object newItemId = table.addItem();
            com.vaadin.data.Item row1 = table.getItem(newItemId);
            row1.getItemProperty("Item").setValue(item);
        }

    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
