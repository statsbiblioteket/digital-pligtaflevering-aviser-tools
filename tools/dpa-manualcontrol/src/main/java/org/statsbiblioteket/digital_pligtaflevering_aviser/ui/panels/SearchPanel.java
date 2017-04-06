package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import java.util.Date;

/**
 * Created by mmj on 3/2/17.
 */
public class SearchPanel extends HorizontalLayout {

    private DateField startDf = new DateField();

    private Button prepareButton = new Button("Prepare month");
    private Button storeTitlesButton = new Button("Initiate");
    private Button saveButton = new Button("Save check");
    private Label info = new Label();

    public SearchPanel() {

        prepareButton.setId("PREPAREBUTTON");
        storeTitlesButton.setId("START");
        saveButton.setId("SAVECHECK");
        startDf.setResolution(DateField.RESOLUTION_MONTH);
        startDf.setValue(new Date());

        this.addComponent(startDf);
        this.addComponent(prepareButton);
        this.addComponent(storeTitlesButton);
        this.addComponent(saveButton);
        this.addComponent(info);
    }

    public Date getSelectedDate() {
        return startDf.getValue();
    }

    public void addClickListener(Button.ClickListener listener) {
        prepareButton.addClickListener(listener);
        storeTitlesButton.addClickListener(listener);
        saveButton.addClickListener(listener);
    }
}
