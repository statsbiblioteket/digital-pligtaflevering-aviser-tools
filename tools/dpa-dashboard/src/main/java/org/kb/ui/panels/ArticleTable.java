package org.kb.ui.panels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Title;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/9/17.
 */
public class ArticleTable extends VerticalLayout {



    Table table;

    public ArticleTable() {



        // Bind a table to it
        table = new Table("files");
        table.addContainerProperty("File", String.class, null);
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

        table.removeAllItems();


        /*Stream<DomsItem> items = fetchStructure.getState(info);



        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                Object newItemId = table.addItem();
                com.vaadin.data.Item row1 = table.getItem(newItemId);
                row1.getItemProperty("Batch").setValue(o.getPath());
            }

        });*/

    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }




}
