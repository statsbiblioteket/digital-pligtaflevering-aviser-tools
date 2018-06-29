package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * DatePanel contains a table which can be used for viewing deliveries plotted into a month-layout
 */
public class EventDatePanel extends VerticalLayout {
    public static final String WEEKNO = "Weekno";
    private final Map<Integer, String> dayMap;
    private Logger log = LoggerFactory.getLogger(getClass());
    private CheckBox checkbox;
    private Table table;
    private TextArea unmappable = new TextArea("");
    private static Button.ClickListener buttonListener;
    private Date month;

    public EventDatePanel() {
        dayMap = new LinkedHashMap<Integer, String>();//LinkedHashMap to keep insertion order
        dayMap.put(Calendar.MONDAY,"Mon");
        dayMap.put(Calendar.TUESDAY,"Tue");
        dayMap.put(Calendar.WEDNESDAY,"Wed");
        dayMap.put(Calendar.THURSDAY,"Thu");
        dayMap.put(Calendar.FRIDAY,"Fri");
        dayMap.put(Calendar.SATURDAY,"Sat");
        dayMap.put(Calendar.SUNDAY,"Sun");
        checkbox = new CheckBox("Visible", true);
        checkbox.setEnabled(false);

        // Bind a table to it
        table = new Table("", null);
        table.addContainerProperty(WEEKNO, String.class, null);

        for (String day : dayMap.values()) {
            table.addContainerProperty(day, String.class, null);
            table.addGeneratedColumn(day, new EventDatePanel.FieldGenerator());
        }
        table.setSortContainerPropertyId(WEEKNO);

        unmappable.setEnabled(false);

        table.setWidth("100%");
        table.setHeightUndefined();
        table.setPageLength(0);
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(checkbox);
        this.addComponent(table);
        this.addComponent(unmappable);
    }



    public void addClickListener(Button.ClickListener buttonListener) {
        this.buttonListener = buttonListener;
    }


    public void setMonth(Date month) {
        this.month = month;
    }


    public void setInfo(Set<String> delStat) {
        table.removeAllItems();

        Item newItemId = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int lowerDatelimit = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int higherDatelimit = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for(int i=lowerDatelimit; i<=higherDatelimit; i++) {
            Calendar day = Calendar.getInstance();
            day.setFirstDayOfWeek(Calendar.MONDAY);
            day.setTime(calendar.getTime());
            day.add(Calendar.DAY_OF_YEAR,i-1);

            String weekday_no = day.get(Calendar.WEEK_OF_YEAR)+"";
            String weekday_name = dayMap.get(day.get(Calendar.DAY_OF_WEEK));

            if (table.getItem(weekday_no) == null) {
                newItemId = table.addItem(weekday_no);
                newItemId.getItemProperty(WEEKNO).setValue(weekday_no);
            }
            newItemId.getItemProperty(weekday_name).setValue(i+ "\n"+"NO DELIVERY");
        }

        for (String item : delStat) {
            try {
                Matcher matcher = UiDataConverter.getPatternMatcher(item);
                if (matcher.matches()) {

                    Calendar day = Calendar.getInstance();
                    day.setFirstDayOfWeek(Calendar.MONDAY);
                    day.setTime(UiDataConverter.getDateFromDeliveryItemDirectoryName(item));

                    String weekday_no = day.get(Calendar.WEEK_OF_YEAR)+"";
                    String weekday_name = dayMap.get(day.get(Calendar.DAY_OF_WEEK));

                    Item tableRow = table.getItem(weekday_no);
                    Property tableCell = tableRow.getItemProperty(weekday_name);

                    Object oldCellValue = tableCell.getValue();

                    if (oldCellValue.toString().contains("NO DELIVERY")) {
                        tableCell.setValue(oldCellValue.toString().replace("NO DELIVERY", item));
                    } else {
                        tableCell.setValue(oldCellValue + "\n" + item);
                    }
                }

            } catch (ParseException e) {
                //Handling of perserexception is done by storing unparsable info to the panel for unmappable values.
                //unmappableValues = unmappableValues.concat(item.getDeliveryName());
                log.error("Strings could not get parsed into Dates in DatePanel", e);
            }
            table.sort();
        }
    }

    /**
     * Set the component to be vieved as enabled in the UI
     *
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /**
     * Set a caption of the embedded Table
     *
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
            VerticalLayout vl = new VerticalLayout();
            Property prop = source.getItem(itemId).getItemProperty(columnId);
            TextArea area = new TextArea(null, prop);
            if (prop.getValue() == null) {
                area.setValue("");
            } else if(prop.getValue().toString().contains("NO DELIVERY")) {
                area.setValue(prop.getValue().toString());
                area.setReadOnly(true);
                vl.addComponent(area);
            } else {

                String[] list = prop.getValue().toString().split("\n");
                for(int rows = 1; rows< list.length; rows++) {
                    area.setValue(prop.getValue().toString());
                    area.setReadOnly(true);
                    Button expectationButton = new Button(list[rows]);
                    expectationButton.setId(list[rows]);
                    expectationButton.addClickListener(buttonListener);
                    vl.addComponent(expectationButton);
                }


            }
            return vl;
        }
    }
}
