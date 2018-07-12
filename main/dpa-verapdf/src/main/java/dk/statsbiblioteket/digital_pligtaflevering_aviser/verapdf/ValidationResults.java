package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import java.util.ArrayList;

/**
 * Result class embedding the list of results from validation of XML from VeraPdf
 * This class contains a list of all broken rules and a summary of the worst rule that is broken
 */
public class ValidationResults {

    private ArrayList<ValidationResult> rulesBroken = new ArrayList<ValidationResult>();
    private SeverenessLevel worstBrokenRule = SeverenessLevel.ACCEPTABLE;

    /**
     * Add an new element to the lest of broken paragraphes
     *
     * @param validationResult
     */
    public void add(ValidationResult validationResult) {
        // if this new ValidationResult is worth than the current vorst validation update validationEnum
        if (worstBrokenRule.compareValidationLevel(validationResult.getValidationEnum())) {
            worstBrokenRule = validationResult.getValidationEnum();
        }
        rulesBroken.add(validationResult);
    }

    /**
     * Get the worst rule that has been broken
     *
     * @return
     */
    public SeverenessLevel getWorstBrokenRule() {
        return worstBrokenRule;
    }

    /**
     * Get a list of all broken rules
     *
     * @return
     */
    public ArrayList<ValidationResult> getRulesBroken() {
        return rulesBroken;
    }
}
