package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import java.util.Date;

/**
 * Panel for handling searching of values for checkings
 */
public class SearchPanel extends HorizontalLayout {

    private DateField startDf = new DateField();
    private CheckBox checked = new CheckBox("Allready validated");

    private Button prepareButton = new Button("Prepare month");
    private Button storeTitlesButton = new Button("Initiate");
    private Button saveButton = new Button("Save check");
    private Button doneButton = new Button("Done del");
    private Label info = new Label();

    public SearchPanel() {

        prepareButton.setId("PREPAREBUTTON");
        storeTitlesButton.setId("START");
        saveButton.setId("SAVECHECK");
        doneButton.setId("SAVEDONE");

        startDf.setResolution(Resolution.MONTH);
        startDf.setValue(new Date());

        this.addComponent(startDf);
        this.addComponent(checked);
        this.addComponent(prepareButton);
        this.addComponent(storeTitlesButton);
        this.addComponent(saveButton);
        this.addComponent(doneButton);
        this.addComponent(info);
    }

    public Date getSelectedDate() {
        return startDf.getValue();
    }

    public boolean useAllreadyValidated() {
        return checked.getValue();
    }

    public void addClickListener(Button.ClickListener listener) {
        prepareButton.addClickListener(listener);
        storeTitlesButton.addClickListener(listener);
        saveButton.addClickListener(listener);
        doneButton.addClickListener(listener);
    }
}
