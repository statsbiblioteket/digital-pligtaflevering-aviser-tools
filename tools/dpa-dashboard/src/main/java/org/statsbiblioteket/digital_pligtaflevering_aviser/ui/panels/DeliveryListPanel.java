package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by mmj on 3/2/17.
 */
public class DeliveryListPanel extends VerticalLayout {

    private Table table;
    private HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();

    public DeliveryListPanel() {

        // Bind a table to it
        table = new Table("Beans of All Sorts");

        table.addContainerProperty("Checked", Boolean.class, null);
        table.addContainerProperty("Date", Date.class, null);
        table.addContainerProperty("Name", String.class, null);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);

        table.setVisibleColumns(new String[]{"Checked", "Date", "Name"});

        //table.setColumnExpandRatio();

        table.setColumnExpandRatio("Checked", 0.1f);
        table.setColumnExpandRatio("Date", 0.5f);
        table.setColumnExpandRatio("Name", 0.4f);

        table.addGeneratedColumn("Checked", new CheckBoxColumnGenerator());

        this.addComponent(table);
    }





    class CheckBoxColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            Property prop = source.getItem(itemId).getItemProperty("Checked"); // if using getItemProperty(columnId) here instead of this the prop will be null
            CheckBox c = new CheckBox(null, prop);
            c.setHeight("13px");
            return c;
        }
    }


    public void setTheStuff(Collection<String> list) {

        itemList.clear();
        table.removeAllItems();

        for(String item : list) {
            Object newItemId = table.addItem();
            Item row1 = table.getItem(newItemId);

            try {
                row1.getItemProperty("Date").setValue(UiDataConverter.getDateFromDeliveryItemDirectoryName(item));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            row1.getItemProperty("Name").setValue(item);
            //itemList.put(row1, o);
        }

    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
