package org.kb.ui.panels;

import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Created by mmj on 3/2/17.
 */
public class SearchPanel extends HorizontalLayout {

    private DateField startDf = new DateField();
    private DateField endDf = new DateField();
    private Button button = new Button("Search");
    private Label info = new Label();

    public SearchPanel() {
        this.addComponent(startDf);
        this.addComponent(endDf);
        this.addComponent(button);
        this.addComponent(info);
    }

    public void addClickListener(Button.ClickListener listener) {
        button.addClickListener(listener);
    }
}
