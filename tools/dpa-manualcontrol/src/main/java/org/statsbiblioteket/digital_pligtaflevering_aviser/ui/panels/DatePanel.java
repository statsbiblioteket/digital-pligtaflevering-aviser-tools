package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * DatePanel contains a table which can be used for viewing deliveries plotted into a month-layout
 */
public class DatePanel extends VerticalLayout {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String[] columns;
    private CheckBox checkbox;
    private BeanItemContainer beans;
    private Table table;
    private TextArea unmappable = new TextArea("");


    public DatePanel() {
        checkbox = new CheckBox("Visible", true);
        checkbox.setEnabled(false);
        beans=new BeanItemContainer(java.time.DayOfWeek.class);

        // Bind a table to it
        table = new Table("", null);

        DayOfWeek[] ds = java.time.DayOfWeek.values();
        columns = new String[ds.length];
        int i = 0;
        table.addContainerProperty("Weekno", String.class, null);
        for(DayOfWeek d : ds) {
            columns[i] = d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            table.addContainerProperty(columns[i], String.class, null);
            table.addGeneratedColumn(columns[i], new DatePanel.FieldGenerator());
            i++;
        }
        table.setSortContainerPropertyId("Weekno");

        unmappable.setEnabled(false);

        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(checkbox);
        this.addComponent(table);
        this.addComponent(unmappable);
    }

    /**
     * Set a list of DeliveryTitleInfo and deploy values into the relevant days in the currently viewed month
     * @param delStat
     */
    public void setInfo(List<DeliveryTitleInfo> delStat) {
        table.removeAllItems();
        Item newItemId = null;
        String unmappableValues = "";
        for(DeliveryTitleInfo item : delStat) {
            try {
                Matcher matcher = UiDataConverter.getPatternMatcher(item.getDeliveryName());
                if (matcher.matches()) {
                    String roundtripValue = matcher.group(2);
                    Date date = UiDataConverter.getDateFromDeliveryItemDirectoryName(item.getDeliveryName());
                    String weekday_name = new SimpleDateFormat("EEE", Locale.ENGLISH).format(date);
                    String weekday_no = new SimpleDateFormat("w", Locale.ENGLISH).format(date);

                    if(table.getItem(weekday_no)==null) {
                        newItemId = table.addItem(weekday_no);
                        newItemId.getItemProperty("Weekno").setValue(weekday_no);
                    }
                    Object oldCellValue = newItemId.getItemProperty(weekday_name).getValue();
                    Object newCellValue = roundtripValue + " - " + item.getNoOfPages() + " - " + item.getNoOfArticles();
                    if(oldCellValue!=null) {
                        newItemId.getItemProperty(weekday_name).setValue(oldCellValue + "\n" + newCellValue);
                    } else {
                        newItemId.getItemProperty(weekday_name).setValue(newCellValue);
                    }

                } else {
                    unmappableValues = unmappableValues.concat(item.getDeliveryName());
                }

            } catch (ParseException e) {
                //Handling of perserexception is done by storing unparsable info to the panel for unmappable values.
                unmappableValues = unmappableValues.concat(item.getDeliveryName());
                log.error("Strings could not get parsed into Dates in DatePanel", e);
            }
            unmappable.setValue(unmappableValues);
            table.sort();
        }
    }

    /**
     * Set the component to be vieved as enabled in the UI
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        beans.removeAllItems();
        super.setEnabled(enabled);
    }

    /**
     * Set a caption of the embedded Table
     * @param caption
     */
    @Override
    public void setCaption(String caption) {
        table.setCaption(caption);
    }

    /**
     * Generate textareas as cells in the table
     */
    static class FieldGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            Property prop = source.getItem(itemId).getItemProperty(columnId);
            TextArea area = new TextArea(null, prop);
            if(prop.getValue() == null) {
                area.setValue("");
            }
            area.setReadOnly(true);
            return area;
        }
    }
}
