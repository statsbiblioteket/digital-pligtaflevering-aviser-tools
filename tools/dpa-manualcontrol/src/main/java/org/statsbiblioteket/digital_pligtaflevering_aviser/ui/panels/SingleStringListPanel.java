package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.Collection;

/**
 * List containing single String items
 */
public class SingleStringListPanel extends VerticalLayout {

    private CheckBox checkbox = new CheckBox("Visible", true);
    private Table table;

    public SingleStringListPanel() {

        checkbox.setEnabled(false);
        // Bind a table to it
        table = new Table("List");
        table.addContainerProperty("Item", String.class, null);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(checkbox);
        this.addComponent(table);
    }

    /**
     * Set a collection of Strings into the embedded table
     * @param list
     */
    public void setTableContent(Collection<String> list) {
        table.removeAllItems();
        for(String item : list) {
            Object newItemId = table.addItem();
            com.vaadin.data.Item row1 = table.getItem(newItemId);
            row1.getItemProperty("Item").setValue(item);
        }
        table.setSortContainerPropertyId("Item");
        table.sort();
    }

    /**
     * Add clicklistener to the embedded table
     * @param listener
     */
    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
