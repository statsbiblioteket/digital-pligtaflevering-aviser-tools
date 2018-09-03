package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Window for administartion of doms-events
 */
public class EventAdminWindow extends Window {

    private final HorizontalLayout contentPanel = new HorizontalLayout();
    private final VerticalLayout vl = new VerticalLayout();
    private final HorizontalLayout hl = new HorizontalLayout();
    private Component resultPanel;

    private final Button override = new Button("Override");
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");
    private final Button stop = new Button("Manually stop");

    public EventAdminWindow(String caption, boolean actionAllowed) {//Manually_stopped
        super(caption);
        if(actionAllowed) {
            override.setId("OVERRIDE");
            delete.setId("DELETE");
            override.setEnabled(!actionAllowed);
            delete.setEnabled(!actionAllowed);
            stop.setId("STOP");
            cancel.setId("CANCELBUTTON");
            cancel.setClickShortcut(ShortcutAction.KeyCode.ENTER);

            hl.addComponent(override);
            hl.addComponent(delete);
            hl.addComponent(stop);
            hl.addComponent(cancel);
        }

        vl.addComponent(contentPanel);
        vl.addComponent(hl);
        super.setContent(vl);
    }

    /**
     * Set listeners to the buttons in the dialog
     * @param listener
     */
    public void setListener(Button.ClickListener listener) {
        override.addClickListener(listener);
        delete.addClickListener(listener);
        cancel.addClickListener(listener);
        stop.addClickListener(listener);
    }

    /**
     * Insert the content to view in the dialog
     * @param content
     */
    public void setDialogContent(Component content) {
        resultPanel = content;
        contentPanel.addComponent(resultPanel);
    }

    /**
     * Insert the content to view in the dialog
     * @param content
     */
    public void setDialogContent(EventPanel content) {
        resultPanel = content;
        content.setClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                override.setEnabled(true);
                delete.setEnabled(true);
            }
        });
        contentPanel.addComponent(resultPanel);
    }
}
