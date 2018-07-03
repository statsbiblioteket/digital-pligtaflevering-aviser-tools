package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

/**
 * Enumeration of rules that has been decided by "digital bevarings gruppen" that we deen to fulfill
 * The level of rules if:
 * Invalid: The document can net be accepted
 * Unknown: A rule has been struck, which there has not been any decision about
 * Manual inspection: The document needs to be inspected manually
 * Approved: The document is ok
 */
public enum SeverenessLevel {
    INVALID, UNKNOWN, MANUAL_INSPECTION, ACCEPTABLE;

    /**
     * Campare broken paragraphes, if comparable is worth than this return true
     * @param comparable
     * @return
     */
    @Deprecated // enums are comparable by default FIXME
    public boolean compareValidationLevel(SeverenessLevel comparable) {
        return this.compareTo(comparable) > 0;
    }
}
