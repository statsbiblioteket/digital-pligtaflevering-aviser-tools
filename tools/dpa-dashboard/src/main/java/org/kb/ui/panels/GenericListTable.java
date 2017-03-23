package org.kb.ui.panels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.Collection;
import java.util.List;

/**
 * Created by mmj on 3/9/17.
 */
public class GenericListTable extends VerticalLayout {

    private BeanItemContainer beans;
    private Table table;

    public GenericListTable(Class c) {
        beans=new BeanItemContainer(c);

        // Bind a table to it
        table = new Table(c.getName(), beans);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(table);
    }


    public void setEnabled(boolean enabled) {
        beans.removeAllItems();
        super.setEnabled(enabled);
    }

    public void setCaption(String caption) {
        table.setCaption(caption);
    }

    public void cleanTable() {
        table.removeAllItems();
    }



    public void setInfo(Collection delStat) {

        beans.removeAllItems();

        for(Object o : delStat) {
            beans.addBean(o);
        }
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
