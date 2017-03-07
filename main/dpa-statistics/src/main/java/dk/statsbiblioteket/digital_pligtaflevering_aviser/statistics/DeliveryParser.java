package dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics;

/**
 * DeliveryParser in a newspaper, Serializable to make it convertible between a stream of xml and an objectmodel
 */
public class DeliveryParser implements java.io.Serializable {

    private String paragraph = null;
    private ValidationResultEnum validationEnum;

    /**
     *
     * @param paragraph
     * @param validationEnum
     */
    public DeliveryParser(String paragraph, ValidationResultEnum validationEnum) {
        this.paragraph = paragraph;
        this.validationEnum = validationEnum;
    }

    public ValidationResultEnum getValidationEnum() {
        return validationEnum;
    }

    /**
     * Enumeration of rules that has been decided by "digital bevarings gruppen" that we deen to fulfill
     * The level of rules if:
     * Invalid: The document can net be accepted
     * Unknown: A rule has been struck, which there has not been any decision about
     * Manual inspection: The document needs to be inspected manually
     * Approved: The document is ok
     */
    public enum ValidationResultEnum {
        INVALID(0), UNKNOWN(10), MANUAL_INSPECTION(20), ACCEPTABLE(30);
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
