package org.kb.ui.windows;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.Page;
import org.kb.ui.datamodel.DeliveryIdentifier;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by mmj on 3/9/17.
 */
public class ResultStorePanel extends VerticalLayout {


    private TextField initials = new TextField();
    private DateField date = new DateField();
    private TextArea area = new TextArea("Check description");


    private BeanItemContainer beans;
    private Table table;

    public ResultStorePanel() {
        super();
        this.setSpacing(true);
        beans=new BeanItemContainer(Page.class);
        // Bind a table to it
        table = new Table("Approved", beans);
        table.setWidth("100%");
        table.setHeight("100%");
        table.setSelectable(true);
        table.setImmediate(true);
        this.addComponent(table);

        // Set the date to present
        date.setValue(new Date());
        area.setRows(10);
        area.setWidth("500px");

        this.addComponent(initials);
        this.addComponent(date);
        this.addComponent(area);

    }

    public void setValues(DeliveryIdentifier item) {
        for(Page o : item.getPages()) {
            beans.addBean(o);
        }
    }

    /*public void setInfo(Collection delStat) {
        beans.removeAllItems();
        for(Object o : delStat) {
            beans.addBean(o);
        }
    }*/


    public void setEnabled(boolean enabled) {
        beans.removeAllItems();
        super.setEnabled(enabled);
    }

    public void setCaption(String caption) {
        table.setCaption(caption);
    }


    public String getInitials() {
        return initials.getValue();
    }

    public String getComment() {
        return area.getValue();
    }

    public void setInfo(Collection delStat) {
        beans.removeAllItems();
        for(Object o : delStat) {
            beans.addBean(o);
        }
    }



    public List getSelections() {
        //Object selectedIds = table.getValue();

        return (List)table.getVisibleItemIds();

    }


    public void addItemClickListener(ItemClickEvent.ItemClickListener listener) {
        table.addItemClickListener(listener);
    }
}
