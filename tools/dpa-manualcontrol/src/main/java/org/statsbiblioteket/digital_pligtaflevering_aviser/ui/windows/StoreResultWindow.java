package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

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
    private final Label chackStateInfo = new Label("Validation can not be performed, since it is already performed");

    public StoreResultWindow(String caption) {
        super(caption);
        ok.setId("OKBUTTON");
        cancel.setId("CANCELBUTTON");

        hl.addComponent(ok);
        hl.addComponent(cancel);
        hl.addComponent(chackStateInfo);
        vl.addComponent(contentPanel);
        vl.addComponent(hl);
        chackStateInfo.setVisible(false);
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
     * Set the dialog to be ready or not ready to performe a press on ok
     * @param ready
     */
    public void setReady(boolean ready) {
        ok.setEnabled(ready);
        chackStateInfo.setVisible(!ready);
    }

    /**
     * Insert UI panels into the dialog
     * @param content
     */
    public void setDialogContent(Layout content) {
        resultPanel = content;
        contentPanel.addComponent(resultPanel);
    }
}
