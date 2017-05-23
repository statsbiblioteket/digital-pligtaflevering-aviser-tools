package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.ui.DateField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.util.Date;

/**
 * This panel gives the possibility of setting information ato get written when a delivery is marked as confirmed.
 */
public class DeliveryConfirmPanel extends VerticalLayout {

    private TextField initials = new TextField();
    private DateField date = new DateField();
    private TextArea area = new TextArea("Check description");

    public DeliveryConfirmPanel() {
        super();
        this.setSpacing(true);

        // Set the date to present
        date.setValue(new Date());
        area.setRows(10);
        area.setWidth("500px");

        this.addComponent(initials);
        this.addComponent(date);
        this.addComponent(area);

    }

    public String getInitials() {
        return initials.getValue();
    }

    public String getComment() {
        return area.getValue();
    }

}
