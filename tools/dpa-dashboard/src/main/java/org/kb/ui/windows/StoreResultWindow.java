package org.kb.ui.windows;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Created by mmj on 3/28/17.
 */
public class StoreResultWindow extends Window {

    private final HorizontalLayout contentPanel = new HorizontalLayout();
    private final VerticalLayout vl = new VerticalLayout();
    private final HorizontalLayout hl = new HorizontalLayout();

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


    public void setDialogContent(Component content) {
        contentPanel.addComponent(content);
    }
}
