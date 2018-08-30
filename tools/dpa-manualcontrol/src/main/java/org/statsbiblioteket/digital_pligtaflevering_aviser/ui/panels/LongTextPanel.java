package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.ui.Button;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

/**
 * Component for viewing long texts
 */
public class LongTextPanel extends VerticalLayout {

    private String panelContent;
    private TextArea field = new TextArea();
    private Button showAll = new Button("Show full text");

    public LongTextPanel(String panelContent) {
        field.setWidth(1000, Unit.PIXELS);
        field.setRows(40);
        this.addComponent(field);
        this.panelContent = panelContent;
        if(this.panelContent.length()>20000) {
            field.setValue(this.panelContent.substring(0,10000));
            this.addComponent(showAll);
            showAll.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    field.setValue(panelContent);
                }
            });
        } else {
            field.setValue(this.panelContent);
        }
        field.setEnabled(true);
    }
}
