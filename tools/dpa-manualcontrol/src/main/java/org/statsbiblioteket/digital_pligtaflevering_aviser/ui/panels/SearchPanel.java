package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import java.util.Date;

/**
 * Panel for handling searching of values for checkings
 */
public class SearchPanel extends HorizontalLayout {

    public static final String prepareButtonId = "PREPAREBUTTON";
    public static final String startButtonId = "START";
    public static final String linkButtonId = "LINK";

    private HorizontalLayout controlLayout = new HorizontalLayout();

    private DateField startDf = new DateField();

    private Button prepareButton = new Button("Prepare month");
    private Button storeTitlesButton = new Button("Start");
    private Button getLink = new Button("Get link");
    private Label info = new Label("");

    public SearchPanel() {

        prepareButton.setId(prepareButtonId);
        storeTitlesButton.setId(startButtonId);
        getLink.setId(linkButtonId);

        startDf.setResolution(Resolution.MONTH);
        startDf.setValue(new Date());

        controlLayout.addComponent(startDf);
        controlLayout.addComponent(prepareButton);
        controlLayout.addComponent(storeTitlesButton);

        this.addComponent(controlLayout);
        this.setComponentAlignment(controlLayout, Alignment.MIDDLE_LEFT);
        this.addComponent(info);
        this.addComponent(getLink);
        this.setComponentAlignment(getLink, Alignment.MIDDLE_RIGHT);
        this.setWidth("100%");
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
