package org.kb.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import org.kb.ui.FetchEventStructure;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class DeliveryListPanel extends VerticalLayout {

    Table table;

    HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();

    public DeliveryListPanel() {



        // Bind a table to it
        table = new Table("Beans of All Sorts");

        table.addContainerProperty("Batch", String.class, null);



        table.setWidth("100%");
        table.setSelectable(true);
        table.setImmediate(true);


        this.addComponent(table);
    }

    /*

        DomsItem titleItem = items.iterator().next();

        //DeliveryStatistics deliveryStatistics = parser.processDomsIdToStream().apply(titleItem);
     */

    public void setInfo(FetchEventStructure fetchStructure, String info) {


        Stream<DomsItem> items = fetchStructure.getState(info);
        itemList.clear();
        table.removeAllItems();

        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                Object newItemId = table.addItem();
                com.vaadin.data.Item row1 = table.getItem(newItemId);
                row1.getItemProperty("Batch").setValue(o.getPath());
                itemList.put(row1, o);
            }

        });

    }

    public DomsItem getDomsItem(com.vaadin.data.Item item) {
        return itemList.get(item);
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }




}
