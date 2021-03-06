package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * Panel for the viewing of newspaper-deliveries on the delivery format.
 * This is the format it is stored on fedora:
 * Delivery -> Title -> Section -> Pages&articles
 */
public class DeliveryListPanel extends VerticalLayout {
    private Logger log = LoggerFactory.getLogger(getClass());

    private CheckBox checkbox = new CheckBox("Visible", true);
    private Table table;
    private HashMap<Item, DomsItem> itemList = new HashMap<Item, DomsItem>();

    public DeliveryListPanel() {

        checkbox.setEnabled(false);
        // Bind a table to it
        table = new Table("Unconfirmed deliveries");

        table.addContainerProperty("Date", Date.class, null);
        table.addContainerProperty("Name", String.class, null);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);

        table.setVisibleColumns(new String[]{"Date", "Name"});
        table.addGeneratedColumn("Date", new DeliveryListPanel.DateColumnGenerator());
        this.addComponent(checkbox);
        this.addComponent(table);
    }


    /**
     * Insert values into the table
     * @param list
     */
    public void setValues(Collection<String> list) {
        itemList.clear();
        table.removeAllItems();

        for (String item : list) {
            Object newItemId = table.addItem();
            Item row1 = table.getItem(newItemId);
            try {
                row1.getItemProperty("Date").setValue(UiDataConverter.getDateFromDeliveryItemDirectoryName(item));
            } catch (ParseException e) {
                Notification.show("The application could not parse the datestring, please contact support", Notification.Type.ERROR_MESSAGE);
                log.error("The date could not be parsed in the DatePanel", e);
            }
            row1.getItemProperty("Name").setValue(item);
        }
        table.setSortContainerPropertyId("Date");
        table.sort();
    }

    /**
     * Add clickListener to the embedded table
     * @param listener
     */
    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }

    public class DateColumnGenerator implements Table.ColumnGenerator {
            @Override
            public Component generateCell(Table source, Object itemId, Object columnId) {
                Property<?> prop = source.getItem(itemId).getItemProperty(columnId);
                if (prop.getType().equals(Date.class)) {
                    Date date = (Date) prop.getValue();
                    if(date==null) {
                        return null;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
                    return new Label(sdf.format(date));
                }
                return null;
            }
        }
}
