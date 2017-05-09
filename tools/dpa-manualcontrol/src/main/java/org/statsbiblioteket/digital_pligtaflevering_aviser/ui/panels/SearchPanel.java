package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import java.util.Date;

/**
 * Panel for handling searching of values for checkings
 */
public class SearchPanel extends HorizontalLayout {

    private DateField startDf = new DateField();

    private Button prepareButton = new Button("Prepare month");
    private Button storeTitlesButton = new Button("Initiate");
    private Button getLink = new Button("Get link");
    private Label info = new Label();

    public SearchPanel() {

        prepareButton.setId("PREPAREBUTTON");
        storeTitlesButton.setId("START");
        getLink.setId("LINK");

        startDf.setResolution(Resolution.MONTH);
        startDf.setValue(new Date());

        this.addComponent(startDf);
        this.addComponent(prepareButton);
        this.addComponent(storeTitlesButton);
        this.addComponent(getLink);
        this.addComponent(info);
    }

    /**
     * Get the dete that is currently shown in the datecomponent
     * @return
     */
    public Date getSelectedDate() {
        return startDf.getValue();
    }

    public void setSelectedMonth(Date month) {
        startDf.setValue(month);
    }

    public void setLabel(String label) {
        info.setValue(label);
    }


    /**
     * Add listeners to buttons in panel
     * @param listener
     */
    public void addClickListener(Button.ClickListener listener) {
        prepareButton.addClickListener(listener);
        storeTitlesButton.addClickListener(listener);
        getLink.addClickListener(listener);
    }
}
