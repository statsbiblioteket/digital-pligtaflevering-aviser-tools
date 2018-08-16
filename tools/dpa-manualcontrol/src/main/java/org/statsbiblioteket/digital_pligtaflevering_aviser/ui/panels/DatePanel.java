package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.WeekPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;


import java.text.ParseException;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * DatePanel contains a table which can be used for viewing deliveries plotted into a month-layout
 */
public class DatePanel extends VerticalLayout {
    public static final String WEEKNO = "Weekno";
    private final Map<Integer, DayOfWeek> dayMap;
    private Logger log = LoggerFactory.getLogger(getClass());
    private CheckBox checkbox;
    private Table table;
    private TextArea unmappable = new TextArea("");

    public DatePanel() {
        dayMap = new LinkedHashMap<>();//LinkedHashMap to keep insertion order
        dayMap.put(Calendar.MONDAY,DayOfWeek.MONDAY);
        dayMap.put(Calendar.TUESDAY,DayOfWeek.TUESDAY);
        dayMap.put(Calendar.WEDNESDAY,DayOfWeek.WEDNESDAY);
        dayMap.put(Calendar.THURSDAY,DayOfWeek.THURSDAY);
        dayMap.put(Calendar.FRIDAY,DayOfWeek.FRIDAY);
        dayMap.put(Calendar.SATURDAY,DayOfWeek.SATURDAY);
        dayMap.put(Calendar.SUNDAY,DayOfWeek.SUNDAY);
        checkbox = new CheckBox("Visible", true);
        checkbox.setEnabled(false);

        // Bind a table to it
        table = new Table("", null);
        table.addContainerProperty(WEEKNO, String.class, null);

        for (DayOfWeek day : dayMap.values()) {
            table.addContainerProperty(day, String.class, null);
            table.addGeneratedColumn(day, new DatePanel.FieldGenerator());
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

    Date month;

    public void setMonth(Date month) {
        this.month = month;
    }

    /**
     * Set a list of DeliveryTitleInfo and deploy values into the relevant days in the currently viewed month
     *
     * @param delStat
     */
    public void setInfo(List<DeliveryTitleInfo> delStat, WeekPattern deliveryPattern) {
        table.removeAllItems();


        for(Object header : table.getVisibleColumns()) {
            FieldGenerator fieldGenerator = (FieldGenerator)table.getColumnGenerator(header);
            if(fieldGenerator!=null) {
                fieldGenerator.setPattern(deliveryPattern);
            }
        }
        Item newItemId = null;
        String unmappableValues = "";

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
            DayOfWeek weekday_name = dayMap.get(day.get(Calendar.DAY_OF_WEEK));

            if (table.getItem(weekday_no) == null) {
                newItemId = table.addItem(weekday_no);
                newItemId.getItemProperty(WEEKNO).setValue(weekday_no);
            }
            newItemId.getItemProperty(weekday_name).setValue(i+ "\n"+"NO DELIVERY");
        }

        for (DeliveryTitleInfo item : delStat) {
            try {
                Matcher matcher = UiDataConverter.getPatternMatcher(item.getDeliveryName());
                if (matcher.matches()) {
                    String roundtripValue = matcher.group(2);

                    Calendar day = Calendar.getInstance();
                    day.setFirstDayOfWeek(Calendar.MONDAY);
                    day.setTime(UiDataConverter.getDateFromDeliveryItemDirectoryName(item.getDeliveryName()));

                    String weekday_no = day.get(Calendar.WEEK_OF_YEAR)+"";
                    DayOfWeek weekday_name = dayMap.get(day.get(Calendar.DAY_OF_WEEK));

                    Item tableRow = table.getItem(weekday_no);
                    Property tableCell = tableRow.getItemProperty(weekday_name);

                    Object oldCellValue = tableCell.getValue();
                    String newCellValue = "";
                    if(item.getNoOfPages()==0 ) {
                        newCellValue = "NO PAGES";
                    } else {
                        newCellValue = "rt-"+roundtripValue + ": pages=" + item.getNoOfPages() + ", articles=" + item.getNoOfArticles();
                    }

                    if (oldCellValue.toString().contains("NO DELIVERY")) {
                        tableCell.setValue(oldCellValue.toString().replace("NO DELIVERY", newCellValue));
                    } else {
                        tableCell.setValue(oldCellValue + "\n" + newCellValue);
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
     * Generate content for the date-panel.
     * The content is of graphical form, and it is based on the allready inserted text.
     */
    static class FieldGenerator implements Table.ColumnGenerator {
        private WeekPattern expected;


        public void setPattern(WeekPattern expected) {
            this.expected = expected;
        }

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            VerticalLayout vl = new VerticalLayout();
            Property prop = source.getItem(itemId).getItemProperty(columnId);
            TextArea area = new TextArea(null, prop);
            Object value = prop.getValue();
            if (value == null || "".equals(value)) {
                area.setValue("");
                return vl;
            }
            area.setReadOnly(true);
            vl.addComponent(area);

            Button contentButton;
            if(value.toString().contains("NO PAGES")) {
                contentButton = new Button(new ThemeResource("icons/empty.png"));
                contentButton.setDescription("The delivery for this newspapertitle has no pages");
                vl.addComponent(contentButton);
            } else if(value.toString().contains("NO DELIVERY")) {
                contentButton = new Button(new ThemeResource("icons/missing.png"));
                contentButton.setDescription("The delivery has not been received");
                vl.addComponent(contentButton);
            }  else {
                contentButton = new Button(new ThemeResource("icons/full.png"));
                contentButton.setDescription("The delivery for this newspapertitle contains 1+ newspaperpages");
                vl.addComponent(contentButton);
            }
            Button expectationButton;
            if (expected == null) {
                expectationButton = new Button(new ThemeResource("icons/unknown.png"));
                expectationButton.setDescription("Unknown newspaper title received");
                vl.addComponent(expectationButton);
            } else {
                String name = columnId.toString();
                DayOfWeek weekDay = DayOfWeek.valueOf(name);
                boolean dayState = expected.getDayState(weekDay);
                if(!Boolean.TRUE.equals(dayState)) {
                    expectationButton = new Button(new ThemeResource("icons/empty.png"));
                    expectationButton.setDescription("There should be NO delivery for this newspapertitle");
                    vl.addComponent(expectationButton);
                } else {
                    expectationButton = new Button(new ThemeResource("icons/full.png"));
                    expectationButton.setDescription("There should be a delivery for this newspapertitle");
                    vl.addComponent(expectationButton);
                }
            }
            return vl;
        }
    }
}
