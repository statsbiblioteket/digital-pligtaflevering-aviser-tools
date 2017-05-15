package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.panels;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel.DataModel;

/**
 * ConfigPanel is used for administration of the system, currently it can be used for flearing files wich has been cashed on the servers filesystem
 */
public class ConfigPanel extends DeliveryValidationPanel {
    private Logger log = LoggerFactory.getLogger(getClass());

    private Button clearDeliveryButton = new Button("Clear");

    public ConfigPanel(DataModel model) {
        super(model);
        super.initialLayout();
        clearDeliveryButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                try {
                    model.removeCurrentSelectedTitleInDelivery();
                } catch (Exception e) {
                    Notification.show("The application has hit an unexpected incedent, please contact support", Notification.Type.ERROR_MESSAGE);
                    log.error(e.getMessage(), e);
                }
            }});
        buttonLayout.addComponent(clearDeliveryButton);
    }
}
