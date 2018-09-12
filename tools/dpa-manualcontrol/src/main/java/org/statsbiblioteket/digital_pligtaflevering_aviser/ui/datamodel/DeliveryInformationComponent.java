package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

/**
 * DTO class containing a validation-state of a delivery
 */
public class DeliveryInformationComponent {

    private String deliveryName = "";
    private ValidationState validationState;

    public DeliveryInformationComponent(String deliveryName, ValidationState validationState) {
        this.deliveryName = deliveryName;
        this.validationState = validationState;
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public ValidationState getValidationState() {
        return validationState;
    }

    public enum ValidationState {
        FAIL, PROGRESS, MANUAL_QA_COMPLETE, APPROVED, DATE;
    }
}
