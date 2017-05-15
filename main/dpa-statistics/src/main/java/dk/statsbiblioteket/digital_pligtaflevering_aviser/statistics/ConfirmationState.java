package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

/**
 * Enums to indicate different validation states for items in a newspaperdelivery
 */
public enum ConfirmationState {
    UNCHECKED, // The value before the user has performed a check of a page or article
    CHECKED, // The value when the user has performed a check of a page or article
    REJECTED; // The value when the user has indicated that there is something wrong with a page or article
}
