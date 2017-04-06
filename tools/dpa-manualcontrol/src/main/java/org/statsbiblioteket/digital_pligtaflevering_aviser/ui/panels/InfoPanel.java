package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.FetchEventStructure;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by mmj on 3/2/17.
 */
public class InfoPanel extends Table {

    public InfoPanel() {
        super("Info");
        this.addContainerProperty("Batch", String.class, null);
        this.setWidth("100%");
    }

    public void setInfo(FetchEventStructure fetchStructure, String info) {

        Stream<DomsItem> items = fetchStructure.getState(info);
        items.forEach(new Consumer<DomsItem>() {
            @Override
            public void accept(final DomsItem o) {
                Object newItemId = addItem();
                com.vaadin.data.Item row1 = getItem(newItemId);
                row1.getItemProperty("Batch").setValue(o.getPath());
            }
        });
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        super.addItemClickListener(listener);
    }
}
