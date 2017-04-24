package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DeliveryTitleInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mmj on 4/20/17.
 */
public class DatePanel extends VerticalLayout {

    private String[] columns;
    private CheckBox checkbox;
    private BeanItemContainer beans;
    private Table table;


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
            i++;
        }

        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(checkbox);
        this.addComponent(table);
    }


    public void setInfo(List<DeliveryTitleInfo> delStat) {
        table.removeAllItems();
        Item newItemId = null;
        for(DeliveryTitleInfo item : delStat) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                Pattern pattern = Pattern.compile("dl_(.*)_rt([0-9]+)$");
                Matcher matcher = pattern.matcher(item.getDeliveryName());
                if (matcher.matches()) {
                    String datePart = matcher.group(1);
                    String roundtripValue = matcher.group(2);
                    Date date = formatter.parse(datePart);
                    String weekday_name = new SimpleDateFormat("EEE", Locale.ENGLISH).format(date);
                    String weekday_no = new SimpleDateFormat("w", Locale.ENGLISH).format(date);

                    if(table.getItem(weekday_no)==null) {
                        newItemId = table.addItem(weekday_no);
                        newItemId.getItemProperty("Weekno").setValue(weekday_no);
                    }
                    newItemId.getItemProperty(weekday_name).setValue(roundtripValue + " - " + item.getNoOfPages() + " - " + item.getNoOfArticles());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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
}
