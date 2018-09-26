package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.EventDTO;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels.LongTextPanel;


import java.util.List;

/**
 * This panel contains all deliveries in a calender-map, and it is possible to get information about the occoured events.
 */
public class EventPanel extends VerticalLayout {

    private TextField initials = new TextField("Initials");
    private BeanItemContainer articleBeans;
    private Table eventTable;
    private Button.ClickListener listener;

    public EventPanel() {
        super();
        this.setSpacing(true);
        initials.setEnabled(false);
        articleBeans = new BeanItemContainer(EventDTO.class);

        // Bind a table to it
        eventTable = new Table("checked articles", articleBeans);
        eventTable.setVisibleColumns( new Object[] {"date", "eventID", "success", "details"} );
        eventTable.addGeneratedColumn("success", new EventPanel.FieldGenerator());
        eventTable.addGeneratedColumn("details", new EventPanel.DetailsFieldGenerator());
        eventTable.setWidth("100%");
        eventTable.setHeight("150px");
        eventTable.setSelectable(true);
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

    public void setClickListener(ItemClickEvent.ItemClickListener listener) {
        eventTable.addItemClickListener(listener);
    }

    /**
     * Set values to be viewed in the panel
     */
    public void setValues(List<EventDTO> items) {

        for (EventDTO o : items) {
            articleBeans.addBean(o);
        }
        eventTable.sort(new String[]{"date"}, new boolean[]{Boolean.FALSE});
    }

    public Object getSelection() {
        return eventTable.getValue();
    }


    public void addButtonEventListener(Button.ClickListener listener) {
        this.listener = listener;
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
     * generate failstate as a button
     */
    class FieldGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            Property prop = source.getItem(itemId).getItemProperty(columnId);
            Object propertyValue = prop.getValue();
            Button expectationButton = new Button();
            if(propertyValue!=null) {
                ThemeResource themeRecourse = ((Boolean)propertyValue).booleanValue() ?
                        new ThemeResource("icons/events/outcome_success.png") : new ThemeResource("icons/events/outcome_failure.png");
                expectationButton = new Button(themeRecourse);
                expectationButton.setId(((EventDTO)itemId).getEventID());
                expectationButton.addClickListener(listener);
            } else {
                expectationButton = new Button(new ThemeResource("icons/events/outcome_failure.png"));
            }
            return expectationButton;
        }
    }

    /**
     * Generate button for viewing detail
     */
    class DetailsFieldGenerator implements Table.ColumnGenerator {

        @Override
        public Component generateCell(Table source, Object itemId, Object columnId) {
            Button button = new Button("details: "+ source.getItem(itemId).getItemProperty(columnId).getValue().toString().length());

            button.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {
                    Property prop = source.getItem(itemId).getItemProperty(columnId);
                    Object propertyValue = prop.getValue();
                    LongTextPanel field = new LongTextPanel(propertyValue.toString());

                    EventAdminWindow textDialog = new EventAdminWindow(clickEvent.getButton().getId(), false);
                    textDialog.setDialogContent(field);
                    textDialog.setModal(true);
                    UI.getCurrent().addWindow(textDialog);

                    textDialog.addCloseListener(new Window.CloseListener() {
                        // inline close-listener
                        @Override
                        public void windowClose(Window.CloseEvent e) {
                            UI.getCurrent().removeWindow(textDialog);
                        }
                    });
                }
            });

            return button;
        }
    }
}
