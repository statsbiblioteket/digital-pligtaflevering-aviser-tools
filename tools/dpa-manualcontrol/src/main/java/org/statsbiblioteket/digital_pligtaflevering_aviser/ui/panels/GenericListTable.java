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
 * Generic table for viewing a table in the table-bean form.
 * This component contains a table and a checkbox which makes it possible to make the table visible/invisible
 */
public class GenericListTable extends VerticalLayout {

    private String checkedColumnName;
    private String sortColumnName = null;
    private Object checkedColumnDefaultValue;

    private CheckBox checkbox;
    private BeanItemContainer beans;
    private Table table;

    /**
     * Construct the TableViewing-component with specific parameters about layout
     * @param c
     * @param checkedColumn
     * @param checkedDefaultValue
     * @param visibleColumns
     * @param tableId
     * @param initialVisible
     */
    public GenericListTable(Class c, String checkedColumn, Object checkedDefaultValue, String[] visibleColumns, String tableId, boolean initialVisible) {
        checkbox = new CheckBox("Visible", initialVisible);
        beans=new BeanItemContainer(c);

        // Bind a table to it
        table = new Table(c.getSimpleName(), beans);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(checkbox);
        checkbox.addValueChangeListener(event ->
                table.setVisible((Boolean)event.getProperty().getValue())
        );
        this.addComponent(table);
        checkedColumnName = checkedColumn;
        checkedColumnDefaultValue = checkedDefaultValue;
        if(checkedColumnName!=null) {
            table.addGeneratedColumn(checkedColumnName, new GenericListTable.CheckBoxColumnGenerator());
        }

        if(visibleColumns!=null) {
            table.setVisibleColumns(visibleColumns);
        }
        table.setId(tableId);
        table.setColumnExpandRatio(checkedColumn, 0.3f);
        table.setVisible(initialVisible);
    }

    /**
     * Set a list of which columns in the table to make visible
     * @param visibleColumns
     */
    public void setVisibleColumns(String[] visibleColumns) {
        if(visibleColumns!=null) {
            table.setVisibleColumns(visibleColumns);
        }
    }

    /**
     * Set the name of the column in the table to sort the rows with
     * @param param
     */
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

    /**
     * Iterate through all rows and find out if all checkboxes is selected
     * @return
     */
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

    /**
     * Set the component to be vieved as enabled in the UI
     * @param enabled
     */
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

    /**
     * Set data to the embedded table
     * @param dataCollection
     */
    public void setInfo(Collection dataCollection) {
        beans.removeAllItems();
        for(Object rowItem : dataCollection) {
            beans.addBean(rowItem);
        }
        if(sortColumnName!= null) {
            table.setSortContainerPropertyId(sortColumnName);
            table.sort();
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
                boolean checkValue = !checkedColumnDefaultValue.equals(oo);
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