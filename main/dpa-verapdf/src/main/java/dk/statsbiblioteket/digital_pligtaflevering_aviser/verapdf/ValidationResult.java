package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

/**
 * Result class embedding the result from validation of XML from VeraPdf
 */
public class ValidationResult {

    private String paragraph = null;
    private ValidationResultEnum validationEnum;

    /**
     *
     * @param paragraph
     * @param validationEnum
     */
    public ValidationResult(String paragraph, ValidationResultEnum validationEnum) {
        this.paragraph = paragraph;
        this.validationEnum = validationEnum;
    }

    public String getParagraph() {
        return paragraph;
    }

    public ValidationResultEnum getValidationEnum() {
        return validationEnum;
    }

    public enum ValidationResultEnum {
        invalid(0), unknown(10), manualInspection(20), approved(30);
        private int validationLevel;

        ValidationResultEnum(int validationLevel) {
            this.validationLevel = validationLevel;
        }

        public int getValidationLevel() {
            return validationLevel;
        }

        /**
         * Campare broken paragraphes, if comparable is worth than this return true
         * @param comparable
         * @return
         */
        public boolean compareValidationLevel(ValidationResultEnum comparable) {
            return validationLevel > comparable.getValidationLevel();
        }
    }
}
