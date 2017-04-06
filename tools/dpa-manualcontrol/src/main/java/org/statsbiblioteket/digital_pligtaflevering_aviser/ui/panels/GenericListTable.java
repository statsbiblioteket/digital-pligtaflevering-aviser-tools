package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.List;

/**
 * Created by mmj on 3/9/17.
 */
public class GenericListTable extends VerticalLayout {

    private String checkedColumnName;
    private String[] columnFilter;
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

    public GenericListTable(Class c, String checkedColumn, String[] visibleColumns, String tableId) {
        this(c);
        checkedColumnName = checkedColumn;
        if(checkedColumnName!=null) {
            table.addGeneratedColumn(checkedColumnName, new GenericListTable.CheckBoxColumnGenerator());
        }
        columnFilter = visibleColumns;
        if(visibleColumns!=null) {
            table.setVisibleColumns(visibleColumns);
        }
        table.setId(tableId);
    }

    public void setVisibleColumns(String[] visibleColumns) {
        columnFilter = visibleColumns;
        if(visibleColumns!=null) {
            table.setVisibleColumns(visibleColumns);
        }
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


    class CheckBoxColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            Property prop = source.getItem(itemId).getItemProperty(checkedColumnName);
            CheckBox c = new CheckBox(null, prop);
            c.setReadOnly(true);
            c.setHeight("13px");
            return c;
        }
    }

    public Object getSelection() {
        Object selectedIds = table.getValue();
        return selectedIds;
    }


    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
