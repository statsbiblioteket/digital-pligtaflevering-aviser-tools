package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.EventDatePanel;

import java.util.List;

/**
 * This panel contains all deliveries in a calender-map, and it is possible to get information about the occoured events.
 */
public class EventPanel extends VerticalLayout {

    private TextField initials = new TextField("Initials");
    private BeanItemContainer articleBeans;
    private Table eventTable;

    public EventPanel() {
        super();
        this.setSpacing(true);
        initials.setEnabled(false);
        articleBeans = new BeanItemContainer(dk.statsbiblioteket.medieplatform.autonomous.Event.class);

        // Bind a table to it
        eventTable = new Table("checked articles", articleBeans);
        eventTable.setVisibleColumns( new Object[] {"date", "eventID", "success", "details"} );
        eventTable.addGeneratedColumn("success", new EventPanel.FieldGenerator());
        eventTable.setWidth("100%");
        eventTable.setHeight("150px");
        eventTable.setSelectable(true);
        eventTable.setImmediate(true);
        this.addComponent(eventTable);
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
        return eventTable.getValue();
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


    /**
     * Generate textareas as cells in the table
     */
    static class FieldGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            VerticalLayout vl = new VerticalLayout();
            Property prop = source.getItem(itemId).getItemProperty(columnId);
            Object propertyValue = prop.getValue();
            Button expectationButton = new Button();
            if(propertyValue!=null) {
                ThemeResource themeRecourse = ((Boolean)propertyValue).booleanValue() ?
                        new ThemeResource("icons/accept.png") : new ThemeResource("icons/fail.png");
                expectationButton = new Button(themeRecourse);
            } else {
                expectationButton = new Button(new ThemeResource("icons/fail.png"));
            }
            return expectationButton;
        }
    }
}
