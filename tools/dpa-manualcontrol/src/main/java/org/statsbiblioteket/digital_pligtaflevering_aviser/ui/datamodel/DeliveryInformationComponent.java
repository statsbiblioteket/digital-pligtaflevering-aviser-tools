package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.datamodel;

/**
 * DTO class containing a validation-state of a delivery
 */
public class DeliveryInformationComponent {

    private String deliveryName = "";
    private ValidationState validationState;
    private boolean overridden = false;

    public DeliveryInformationComponent(String deliveryName, ValidationState validationState) {
        this.deliveryName = deliveryName;
        this.validationState = validationState;
    }
    
    public DeliveryInformationComponent(String deliveryName, ValidationState validationState, boolean overridden) {
        this(deliveryName, validationState);
        this.overridden = overridden;
    }
    
    
    public String getDeliveryName() {
        return deliveryName;
    }

    public ValidationState getValidationState() {
        return validationState;
    }
    
    public boolean isOverridden() {
        return overridden;
    }
    
    public enum ValidationState {
        FAIL, PROGRESS, MANUAL_QA_COMPLETE, APPROVED, STOPPED, DATE;
    }
}
