package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.windows;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
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

    public StoreResultWindow(String caption) {
        super(caption);
        ok.setId("OKBUTTON");
        cancel.setId("CANCELBUTTON");

        hl.addComponent(ok);
        hl.addComponent(cancel);
        vl.addComponent(contentPanel);
        vl.addComponent(hl);

        super.setContent(vl);
    }

    public void setListener(Button.ClickListener listener) {
        ok.addClickListener(listener);
        cancel.addClickListener(listener);
    }

    public void setReady(boolean ready) {
        ok.setEnabled(ready);
    }


    public void setDialogContent(Layout content) {
        resultPanel = content;
        contentPanel.addComponent(resultPanel);
    }
}
