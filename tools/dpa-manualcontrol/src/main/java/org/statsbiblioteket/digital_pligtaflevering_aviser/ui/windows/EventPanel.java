package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.List;

/**
 * This panel contains all deliveries in a calender-map, and it is possible to get information about the occoured events.
 */
public class EventPanel extends VerticalLayout {

    private TextField initials = new TextField("Initials");
    private BeanItemContainer articleBeans;
    private Table articleTable;

    public EventPanel() {
        super();
        this.setSpacing(true);

        articleBeans = new BeanItemContainer(dk.statsbiblioteket.medieplatform.autonomous.Event.class);

        // Bind a table to it
        articleTable = new Table("checked articles", articleBeans);
        articleTable.setVisibleColumns( new Object[] {"date", "eventID", "success", "details"} );
        articleTable.setWidth("100%");
        articleTable.setHeight("150px");
        articleTable.setSelectable(true);
        articleTable.setImmediate(true);
        this.addComponent(articleTable);
        this.addComponent(initials);
    }

    /**
     * Set the initials of the person currently performing the approvement
     * @param defaultInitials
     */
    public void setInitials(String defaultInitials) {
        initials.setValue(defaultInitials);
    }

    /**
     * Set values to be viewed in the panel
     */
    public void setValues(List<dk.statsbiblioteket.medieplatform.autonomous.Event> items) {

        for (dk.statsbiblioteket.medieplatform.autonomous.Event o : items) {
            articleBeans.addBean(o);
        }
    }

    public Object getSelection() {
        return articleTable.getValue();
    }



    @Override
    public void setEnabled(boolean enabled) {
        articleBeans.removeAllItems();
        super.setEnabled(enabled);
    }


    /**
     * Get the initials from the edit-field, the initial value can have been replaced by the user
     * @return
     */
    public String getInitials() {
        return initials.getValue();
    }


}
