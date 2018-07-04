package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


public class EventAdminWindow extends Window {

    private final HorizontalLayout contentPanel = new HorizontalLayout();
    private final VerticalLayout vl = new VerticalLayout();
    private final HorizontalLayout hl = new HorizontalLayout();
    private Layout resultPanel;

    private final Button override = new Button("Override");
    private final Button delete = new Button("Delete");
    private final Button cancel = new Button("Cancel");
    private final TextField securityKey = new TextField();
    private final Label checkStateInfo = new Label("Validation can not be performed, since it is already performed");

    public EventAdminWindow(String caption) {
        super(caption);
        override.setId("OVERRIDE");
        delete.setId("DELETE");
        cancel.setId("CANCELBUTTON");
        cancel.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        hl.addComponent(override);
        hl.addComponent(delete);
        hl.addComponent(cancel);
        hl.addComponent(securityKey);
        hl.addComponent(checkStateInfo);
        vl.addComponent(contentPanel);
        vl.addComponent(hl);
        checkStateInfo.setVisible(false);
        super.setContent(vl);
    }

    public boolean validateSecurityKey() {
        return "S3cr3t".equals(securityKey.getValue());
    }

    /**
     * Set listeners to the buttons in the dialog
     * @param listener
     */
    public void setListener(Button.ClickListener listener) {
        override.addClickListener(listener);
        delete.addClickListener(listener);
        cancel.addClickListener(listener);
    }

    /**
     * Insert the content to view in the dialog
     * @param content
     */
    public void setDialogContent(Layout content) {
        resultPanel = content;
        contentPanel.addComponent(resultPanel);
    }
}
