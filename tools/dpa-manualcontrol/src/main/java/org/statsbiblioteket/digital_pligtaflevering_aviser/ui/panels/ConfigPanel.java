package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.ui.Button;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;

/**
 * Created by mmj on 3/10/17.
 */
public class ConfigPanel extends DeliveryValidationPanel {

    private Button clearDeliveryButton = new Button("Clear");

    public ConfigPanel(DataModel model) {
        super(model);
        super.initialLayout();
        clearDeliveryButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                try {
                    model.removeCurrentSelectedTitleInDelivery();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }});
        buttonLayout.addComponent(clearDeliveryButton);
    }
}
