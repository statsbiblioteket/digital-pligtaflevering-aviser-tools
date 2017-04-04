package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryIdentifier;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.Wrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by mmj on 3/2/17.
 */
public class TitleListPanel extends VerticalLayout {

    private BeanItemContainer<DeliveryIdentifier> beans;
    private Table table;
    private HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();
    private HashSet<String> titleList = new HashSet<String>();

    public TitleListPanel() {
        beans = new BeanItemContainer<DeliveryIdentifier>(DeliveryIdentifier.class);

        // Bind a table to it
        table = new Table("Tilte bean", beans);

        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        //table.setVisibleColumns(new String[]{"title", "noOfArticles", "noOfPages"});

        /*table.setColumnExpandRatio("title", 0.8f);
        table.setColumnExpandRatio("noOfArticles", 0.1f);
        table.setColumnExpandRatio("noOfPages", 0.1f);*/
        this.addComponent(table);
    }

    public void setInfo(List<DeliveryIdentifier> delStat) {
        //table.setCaption(delStat.getDeliveryName());
        beans.removeAllItems();
        //titleList.clear();

        for(DeliveryIdentifier title : delStat) {
            //titleList.add(title.getTitle());
            beans.addBean(title);
        }
        Object o = table.getColumnHeaders();
    }


    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
