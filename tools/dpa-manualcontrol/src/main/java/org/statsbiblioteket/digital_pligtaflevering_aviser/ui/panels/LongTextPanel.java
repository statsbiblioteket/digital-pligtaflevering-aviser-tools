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

        //If the text contains more then 20.000 characters, show the first 10.000
        //The idea is that it is very rare that more then 10.000 characters is needed, and often the text only contains about 100 characters
        if(this.panelContent.length()>20_000) {
            //If the text contains more then 20.000 characters, just show the beginning, and give the user a possibility of showing everything
            field.setValue(this.panelContent.substring(0,10_000));
            this.addComponent(showAll);
            showAll.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    field.setValue(panelContent);
                }
            });
        } else {
            //If the text contains less then 20.000 characters, just show everythin, and no detail-button
            field.setValue(this.panelContent);
        }
        field.setEnabled(true);
    }
}
