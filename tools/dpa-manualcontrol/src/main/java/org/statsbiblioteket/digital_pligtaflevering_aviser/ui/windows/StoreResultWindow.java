package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Window for confirming that the performed checks should be stored in fedora
 */
public class StoreResultWindow extends Window {

    private final HorizontalLayout contentPanel = new HorizontalLayout();
    private final VerticalLayout vl = new VerticalLayout();
    private final HorizontalLayout hl = new HorizontalLayout();
    private Layout resultPanel;

    private final Button ok = new Button("Ok");
    private final Button cancel = new Button("Cancel");
    private final Button force = new Button("Force");
    private final Label checkStateInfo = new Label("Validation can not be performed, since it is already performed");

    public StoreResultWindow(String caption) {
        super(caption);
        ok.setId("OKBUTTON");
        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        cancel.setId("CANCELBUTTON");
        force.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                ok.setEnabled(true);
            }
        });


        hl.addComponent(ok);
        hl.addComponent(cancel);
        hl.addComponent(force);
        hl.addComponent(checkStateInfo);
        vl.addComponent(contentPanel);
        vl.addComponent(hl);
        checkStateInfo.setVisible(false);
        super.setContent(vl);
    }

    /**
     * Set listeners to the buttons in the dialog
     * @param listener
     */
    public void setListener(Button.ClickListener listener) {
        ok.addClickListener(listener);
        cancel.addClickListener(listener);
    }

    /**
     * Set the dialog to be ready or not ready to perform a press on ok.
     * @param ready if the parameter is false the "ok" button is disabled and a comment about why it is disabled is shown
     */
    public void setReady(boolean ready) {
        ok.setEnabled(ready);
        force.setVisible(!ready);
        checkStateInfo.setVisible(!ready);
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
