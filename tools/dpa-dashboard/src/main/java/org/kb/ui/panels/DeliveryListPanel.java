package org.kb.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.kb.ui.FetchEventStructure;
import org.kb.ui.datamodel.UiDataConverter;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class DeliveryListPanel extends VerticalLayout {

    private Table table;
    private HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();

    public DeliveryListPanel() {

        // Bind a table to it
        table = new Table("Beans of All Sorts");
        table.addContainerProperty("Date", Date.class, null);
        table.addContainerProperty("Name", String.class, null);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(table);
    }

    public void setInfo(FetchEventStructure fetchStructure, String info) {

        Stream<DomsItem> items = fetchStructure.getState(info);
        itemList.clear();
        table.removeAllItems();

        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                Object newItemId = table.addItem();
                Item row1 = table.getItem(newItemId);
                try {
                    row1.getItemProperty("Date").setValue(UiDataConverter.getDateFromDeliveryItemDirectoryName(o.getPath()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                row1.getItemProperty("Name").setValue(o.getPath());
                itemList.put(row1, o);
            }
        });
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


    public DomsItem getDomsItem(Item item) {
        return itemList.get(item);
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
