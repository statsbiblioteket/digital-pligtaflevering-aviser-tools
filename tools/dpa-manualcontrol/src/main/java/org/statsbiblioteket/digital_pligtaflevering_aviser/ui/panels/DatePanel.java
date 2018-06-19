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

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * DatePanel contains a table which can be used for viewing deliveries plotted into a month-layout
 */
public class DatePanel extends VerticalLayout {
    public static final String WEEKNO = "Weekno";
    private final HashMap<Integer, String> dayMap;
    private Logger log = LoggerFactory.getLogger(getClass());

    //TODO show year/month in a header
    
    private CheckBox checkbox;
    private BeanItemContainer beans;
    private Table table;
    private TextArea unmappable = new TextArea("");

    public DatePanel() {
        dayMap = new HashMap<Integer, String>();
        dayMap.put(Calendar.MONDAY,"Mon");
        dayMap.put(Calendar.TUESDAY,"Tue");
        dayMap.put(Calendar.WEDNESDAY,"Wed");
        dayMap.put(Calendar.THURSDAY,"Thu");
        dayMap.put(Calendar.FRIDAY,"Fri");
        dayMap.put(Calendar.SATURDAY,"Sat");
        dayMap.put(Calendar.SUNDAY,"Sun");
    
    
    
        checkbox = new CheckBox("Visible", true);
        checkbox.setEnabled(false);
        beans = new BeanItemContainer(java.time.DayOfWeek.class);

        // Bind a table to it
        table = new Table("", null);

        DayOfWeek[] ds = java.time.DayOfWeek.values();
        //columns = new String[ds.length];
        int i = 0;
        table.addContainerProperty(WEEKNO, String.class, null);

        for (DayOfWeek d : ds) {
            //columns[i] = d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            table.addContainerProperty(dayMap.get(Calendar.MONDAY+1), String.class, null);
            table.addGeneratedColumn(dayMap.get(Calendar.MONDAY+1), new DatePanel.FieldGenerator());
            i++;
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
    public void setInfo(List<DeliveryTitleInfo> delStat) {
        table.removeAllItems();
        Item newItemId = null;
        String unmappableValues = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int lowerDatelimit = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int higherDatelimit = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    
    
        for(int i = lowerDatelimit; i <= higherDatelimit; i++) {
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
            newItemId.getItemProperty(weekday_name).setValue(i+"");
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
                    String weekday_name = dayMap.get(day.get(Calendar.DAY_OF_WEEK));

                    Item tableRow = table.getItem(weekday_no);
                    Property tableCell = tableRow.getItemProperty(weekday_name);

                    if (table.getItem(weekday_no) == null) {
                        newItemId = table.addItem(weekday_no);
                        newItemId.getItemProperty(WEEKNO).setValue(weekday_no);
                    }
                    Object oldCellValue = tableCell.getValue();
                    Object newCellValue = "rt-"+roundtripValue + ": pages=" + item.getPages() + ", articles=" + item.getArticles();

                    if (oldCellValue != null) {
                        tableCell.setValue(oldCellValue + "\n" + newCellValue);
                    } else {
                        tableCell.setValue(newCellValue);
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
        beans.removeAllItems();
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
            Property prop = source.getItem(itemId).getItemProperty(columnId);
            TextArea area = new TextArea(null, prop);
            if (prop.getValue() == null) {
                area.setValue("");
            }
            area.setReadOnly(true);
            return area;
        }
    }
}
