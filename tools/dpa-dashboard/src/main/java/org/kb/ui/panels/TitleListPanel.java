package org.kb.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.FetchEventStructure;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class TitleListPanel extends VerticalLayout {

    BeanItemContainer<Title> beans;

    Table table;

    HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();

    public TitleListPanel() {
        beans = new BeanItemContainer<Title>(Title.class);

        // Bind a table to it
        table = new Table("Tilte bean", beans);

        table.setWidth("100%");
        table.setSelectable(true);
        table.setImmediate(true);






        this.addComponent(table);
    }

    /*

        DomsItem titleItem = items.iterator().next();

        //DeliveryStatistics deliveryStatistics = parser.processDomsIdToStream().apply(titleItem);
     */

    public void setInfo(DeliveryStatistics delStat) {

        table.setCaption(delStat.getDeliveryName());
        beans.removeAllItems();

        for(Title title : delStat.getTitles().getTitles()) {

            beans.addBean(title);


            /*Object newItemId = table.addItem();
            com.vaadin.data.Item row1 = table.getItem(newItemId);
            row1.getItemProperty("Batch").setValue(o.getPath());
            itemList.put(row1, o);*/
        }


        //beans.addAll(delStat.getTitles().getTitles());
    }

    public DomsItem getDomsItem(com.vaadin.data.Item item) {
        return itemList.get(item);
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }




}
