package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Wrapper;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by mmj on 3/2/17.
 */
public class TitleListPanel extends VerticalLayout {

    private BeanItemContainer<Title> beans;
    private Table table;
    private HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();
    private HashSet<String> titleList = new HashSet<String>();

    public TitleListPanel() {
        beans = new BeanItemContainer<Title>(Title.class);

        // Bind a table to it
        table = new Table("Tilte bean", beans);

        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        table.setVisibleColumns(new String[]{"title", "noOfArticles", "noOfPages"});

        table.setColumnExpandRatio("title", 0.8f);
        table.setColumnExpandRatio("noOfArticles", 0.1f);
        table.setColumnExpandRatio("noOfPages", 0.1f);
        this.addComponent(table);
    }

    public void setInfo(DeliveryStatistics delStat) {
        table.setCaption(delStat.getDeliveryName());
        beans.removeAllItems();
        titleList.clear();

        for(Title title : delStat.getTitles().getTitles()) {
            titleList.add(title.getTitle());
            beans.addBean(title);
        }
        Object o = table.getColumnHeaders();
    }

    public Wrapper getTitles() {
        Wrapper wrapper = new Wrapper();
        try {


            wrapper.setHashtable(titleList);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return wrapper;
    }


    public DomsItem getDomsItem(com.vaadin.data.Item item) {
        return itemList.get(item);
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
