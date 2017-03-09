package org.kb.ui.panels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;
import org.kb.ui.FetchEventStructure;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class TitleListPanel extends VerticalLayout {

    BeanItemContainer<Title> beans;

    Table table;

    public TitleListPanel() {
        beans =
                new BeanItemContainer<Title>(Title.class);


        // Bind a table to it
        table = new Table("Beans of All Sorts", beans);

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

        beans.addAll(delStat.getTitles().getTitles());



    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }




}
