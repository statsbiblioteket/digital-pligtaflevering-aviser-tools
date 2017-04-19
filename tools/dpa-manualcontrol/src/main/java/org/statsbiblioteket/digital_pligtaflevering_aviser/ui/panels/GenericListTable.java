package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.ConfirmationState;

import java.util.Collection;
import java.util.Iterator;

/**
 * Generic table for viewing a table in the table-bean form
 */
public class GenericListTable extends VerticalLayout {

    private String checkedColumnName;
    private String sortColumnName = null;
    private Object checkedColumnDefaultValue;
    private String[] columnFilter;
    private BeanItemContainer beans;
    private Table table;

    public GenericListTable(Class c) {
        beans=new BeanItemContainer(c);

        // Bind a table to it
        table = new Table(c.getSimpleName(), beans);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(table);
    }

    public GenericListTable(Class c, String checkedColumn, Object checkedDefaultValue, String[] visibleColumns, String tableId) {
        this(c);
        checkedColumnName = checkedColumn;
        checkedColumnDefaultValue = checkedDefaultValue;
        if(checkedColumnName!=null) {
            table.addGeneratedColumn(checkedColumnName, new GenericListTable.CheckBoxColumnGenerator());
        }
        columnFilter = visibleColumns;
        if(visibleColumns!=null) {
            table.setVisibleColumns(visibleColumns);
        }
        table.setId(tableId);
        table.setColumnExpandRatio(checkedColumn, 0.2f);
    }

    public void setVisibleColumns(String[] visibleColumns) {
        columnFilter = visibleColumns;
        if(visibleColumns!=null) {
            table.setVisibleColumns(visibleColumns);
        }
    }

    public void setSortParam(String param) {
        sortColumnName = param;
    }

    /**
     * Check the specified row in the table
     * @param itemId
     * @param value
     */
    public void checkSpecific(Object itemId, Object value) {
        table.getItem(itemId).getItemProperty(checkedColumnName).setValue(value);
        table.refreshRowCache();
    }


    public boolean isAllChecked() {

        Collection i = table.getContainerDataSource().getItemIds();
        Iterator<?> walker=i.iterator();
        while(walker.hasNext()) {
            if(!(Boolean)table.getItem(walker.next()).getItemProperty(checkedColumnName).getValue()) {
                return false;
            }
        }
        return true;
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
        if(sortColumnName!= null) {
            table.setSortContainerPropertyId(sortColumnName);
            table.refreshRowCache();
        }
    }

    public void setValToCheck(Object itemId, Object value) {
        table.getItem(itemId).getItemProperty(checkedColumnName).setValue(value);
    }


    class CheckBoxColumnGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            Property prop = source.getItem(itemId).getItemProperty(checkedColumnName);
            CheckBox c;
            if(checkedColumnDefaultValue!= null) {
                ConfirmationState oo = (ConfirmationState)prop.getValue();
                boolean checkValue = !checkedColumnDefaultValue.equals(oo.name());
                c = new CheckBox(null, checkValue);
            } else {
                c = new CheckBox(null, prop);
            }
            c.setReadOnly(true);
            c.setHeight("13px");
            return c;
        }
    }


    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
