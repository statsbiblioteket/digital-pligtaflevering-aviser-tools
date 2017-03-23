package org.kb.ui.panels;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

/**
 * Created by mmj on 3/9/17.
 */
public class FileListTable extends VerticalLayout {

    private BeanItemContainer beans;
    private Table table;

    public FileListTable(Class c) {
        beans=new BeanItemContainer(c);

        // Bind a table to it
        table = new Table("files", beans);
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

    public void setInfo(List delStat) {

        beans.removeAllItems();

        for(Object o : delStat) {
            beans.addBean(o);
        }
    }

    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
