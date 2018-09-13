package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryInformationComponent;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.UiDataConverter;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryInformationComponent.ValidationState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryInformationComponent.ValidationState.DATE;

/**
 * EventDatePanel contains a table which can be used for viewing deliveries plotted into a month-layout
 */
public class EventDatePanel extends VerticalLayout {
    public static final String WEEKNO = "Weekno";
    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM");
    private final Map<Integer, String> dayMap;
    private Logger log = LoggerFactory.getLogger(getClass());
    private CheckBox checkbox;
    private Table table;
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
            table.addContainerProperty(day, List.class, null);
            table.addGeneratedColumn(day, new EventDatePanel.FieldGenerator());
        }
        table.setSortContainerPropertyId(WEEKNO);
        table.setWidth("100%");
        table.setHeightUndefined();
        table.setPageLength(0);
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(checkbox);
        this.addComponent(table);
    }



    public void addClickListener(Button.ClickListener buttonListener) {
        this.buttonListener = buttonListener;
    }

    /**
     * Set the month to be viewed in the panel
     * @param month
     */
    public void setMonth(Date month) {
        this.month = month;
        table.setCaption(sdf.format(month));
    }

    /**
     * Set the content to be viewed in the calendar
     * @param delStat
     */
    public void setInfo(List<DeliveryInformationComponent> delStat) {
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
            ArrayList itemList = new ArrayList();
            itemList.add(new DeliveryInformationComponent(day.get(Calendar.DAY_OF_MONTH)+"", DATE));
            newItemId.getItemProperty(weekday_name).setValue(itemList);
        }

        for (DeliveryInformationComponent item : delStat) {
            try {
                Matcher matcher = UiDataConverter.getPatternMatcher(item.getDeliveryName());
                if (matcher.matches()) {

                    Calendar day = Calendar.getInstance();
                    day.setFirstDayOfWeek(Calendar.MONDAY);
                    day.setTime(UiDataConverter.getDateFromDeliveryItemDirectoryName(item.getDeliveryName()));

                    String weekday_no = day.get(Calendar.WEEK_OF_YEAR)+"";
                    String weekday_name = dayMap.get(day.get(Calendar.DAY_OF_WEEK));

                    Item tableRow = table.getItem(weekday_no);
                    Property tableCell = tableRow.getItemProperty(weekday_name);

                    List oldCellValue = (List)tableCell.getValue();
                    if (oldCellValue!=null) {
                        oldCellValue.add(item);
                        tableCell.setValue(oldCellValue);
                    }
                }

            } catch (ParseException e) {
                Notification.show("The application can not show all deliveries", Notification.Type.WARNING_MESSAGE);
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
            Object propertyValue = prop.getValue();
            if(propertyValue!=null) {

                List<DeliveryInformationComponent> componentList = (List<DeliveryInformationComponent>)prop.getValue();

                if(componentList.size()==0) {
                    Button expectationButton = new Button("", new ThemeResource("icons/events/unknown.png"));
                    vl.addComponent(expectationButton);
                } else {
                    for(DeliveryInformationComponent deliveryComponent : componentList) {
                        String id = deliveryComponent.getDeliveryName();
                        String name = id;
                        if (deliveryComponent.isOverridden()){
                            name = name+" *";
                        }
                        ValidationState state = deliveryComponent.getValidationState();
    
                        ThemeResource themeRecourse;
                        Button expectationButton;
                        switch(state) {
                            case FAIL:
                                themeRecourse = new ThemeResource("icons/events/fail.png");
                                expectationButton = new Button(name, themeRecourse);
                                expectationButton.setId(id);
                                expectationButton.addClickListener(buttonListener);
                                vl.addComponent(expectationButton);
                                break;
                            case PROGRESS:
                                themeRecourse = new ThemeResource("icons/events/progress.png");
                                expectationButton = new Button(name, themeRecourse);
                                expectationButton.setId(id);
                                expectationButton.addClickListener(buttonListener);
                                vl.addComponent(expectationButton);
                                break;
                            case STOPPED:
                                themeRecourse = new ThemeResource("icons/events/stopped.png");
                                expectationButton = new Button(name, themeRecourse);
                                expectationButton.setId(id);
                                expectationButton.addClickListener(buttonListener);
                                vl.addComponent(expectationButton);
                                break;
                            case MANUAL_QA_COMPLETE:
                                themeRecourse = new ThemeResource("icons/events/manualQA.png");
                                expectationButton = new Button(name, themeRecourse);
                                expectationButton.setId(id);
                                expectationButton.addClickListener(buttonListener);
                                vl.addComponent(expectationButton);
                                break;
                            case APPROVED:
                                themeRecourse = new ThemeResource("icons/events/approved.png");
                                expectationButton = new Button(name, themeRecourse);
                                expectationButton.setId(id);
                                expectationButton.addClickListener(buttonListener);
                                vl.addComponent(expectationButton);
                                break;
                            case DATE:
                                vl.addComponent(new Label(id));
                                break;
                        }
                    }
                }
            }
            return vl;
        }
    }
}
