package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

/**
 * Enumeration of rules that has been decided by "digital bevarings gruppen" that we deen to fulfill
 * The level of rules if:
 * Invalid: The document can not be accepted
 * Unknown: A rule has been struck, which there has not been any decision about
 * Manual inspection: The document needs to be inspected manually
 * Approved: The document is ok
 */
public enum SeverenessLevel {
    //INAVLID - indicates that the pdf-file breaks a paragraph that is in no way accepable
    //UNKNOWN - indicates that the pdf-file breaks a paragraph that is unknown by the system
    //MANUAL_INSPECTION - indicates that the pdf-file breaks a paragraph that makes it nessesary to manually inspect the file
    //ACCEPTABLE - indicates that the pdf-file breaks a paragraph that we consider unproblematic
    //ACCEPTIGNORE - indicates that the pdf-file breaks a paragraph that under the given circumstances is decided to accept
    INVALID(true), UNKNOWN(true), MANUAL_INSPECTION(true), ACCEPTABLE(false), ACCEPTIGNORE(false);

    private final boolean bad;

    /**
     * If this parameter is true the file is registered as incapable for preservation
     * @return
     */
    public boolean isBad() {
        return bad;
    }

    SeverenessLevel(boolean bad) {
        this.bad = bad;
    }

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
