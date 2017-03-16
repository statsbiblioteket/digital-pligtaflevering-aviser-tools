package org.kb.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.kb.ui.FetchEventStructure;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class DeliveryContentPanel extends VerticalLayout {

    private HorizontalLayout buttonLayout = new HorizontalLayout();
    private Table table = new Table("Events");

    public DeliveryContentPanel() {
        table.setCaption("Events");
        table.addContainerProperty("Event", String.class, null);
        table.addContainerProperty("sucess", Boolean.class, null);
        table.addContainerProperty("Date", Date.class, null);
        table.setWidth("100%");
        table.setSelectable(true);
        table.setImmediate(true);

        buttonLayout.addComponent(new Button("B1"));
        buttonLayout.addComponent(new Button("B2"));
        buttonLayout.addComponent(new Button("B3"));

        this.addComponent(buttonLayout);
        this.addComponent(table);
    }

    public void setInfo(FetchEventStructure fetchStructure, String currentEvent, String info) {
        table.getContainerDataSource().removeAllItems();
        table.setCaption(info);
        Stream<DomsItem> items = fetchStructure.getState(currentEvent);


        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {

                if(info.equals(o.getPath())) {

                    List<dk.statsbiblioteket.medieplatform.autonomous.Event> events = o.getOriginalEvents();

                    Iterator<dk.statsbiblioteket.medieplatform.autonomous.Event> it = events.iterator();

                    while(it.hasNext()) {
                        dk.statsbiblioteket.medieplatform.autonomous.Event event = it.next();
                        Object newItemId = table.addItem();
                        com.vaadin.data.Item row1 = table.getItem(newItemId);
                        row1.getItemProperty("Event").setValue(event.getEventID());
                        row1.getItemProperty("sucess").setValue(event.isSuccess());
                        row1.getItemProperty("Date").setValue(event.getDate());
                    }
                }
            }

        });

    }


    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
