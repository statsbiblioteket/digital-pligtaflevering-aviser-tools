package org.kb.ui.panels;

import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.util.Date;

/**
 * Created by mmj on 3/2/17.
 */
public class SearchPanel extends HorizontalLayout {

    private DateField startDf = new DateField();

    private Button searchButton = new Button("Search");
    private Button storeTitlesButton = new Button("store titles");
    private Label info = new Label();

    public SearchPanel() {

        searchButton.setId("SEARCHBUTTON");
        storeTitlesButton.setId("STOREBUTTON");
        startDf.setResolution(DateField.RESOLUTION_MONTH);
        startDf.setValue(new Date());

        this.addComponent(startDf);
        this.addComponent(searchButton);
        this.addComponent(storeTitlesButton);
        this.addComponent(info);
    }

    public Date getSelectedDate() {
        return startDf.getValue();
    }

    public void addClickListener(Button.ClickListener listener) {
        searchButton.addClickListener(listener);
        storeTitlesButton.addClickListener(listener);
    }
}
