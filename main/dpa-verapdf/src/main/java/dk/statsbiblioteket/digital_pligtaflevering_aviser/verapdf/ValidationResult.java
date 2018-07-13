package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

/**
 * Result class embedding the result from validation of XML from VeraPdf
 */
public class ValidationResult {

    private String paragraph = null;
    private SeverenessLevel validationEnum;

    /**
     *
     * @param paragraph
     * @param validationEnum
     */
    public ValidationResult(String paragraph, SeverenessLevel validationEnum) {
        this.paragraph = paragraph;
        this.validationEnum = validationEnum;
    }

    public String getParagraph() {
        return paragraph;
    }

    public SeverenessLevel getValidationEnum() {
        return validationEnum;
    }

}
