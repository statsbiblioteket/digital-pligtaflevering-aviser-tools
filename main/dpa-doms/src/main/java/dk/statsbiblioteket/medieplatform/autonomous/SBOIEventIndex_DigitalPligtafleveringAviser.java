package dk.statsbiblioteket.medieplatform.autonomous;

import java.net.MalformedURLException;

/**
 * This class is a work around that the record base needed in the summa query string is currently hardcoded in
 * SBOIEventIndex in a dependent artifact.  A Provider is necessary as the constructor can throw checked
 * exceptions.
 */
public class SBOIEventIndex_DigitalPligtafleveringAviser<T extends Item> extends SBOIEventIndex<T> {

    protected org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private final String recordBase;

    public SBOIEventIndex_DigitalPligtafleveringAviser(
            String summaLocation,
            PremisManipulatorFactory<T> premisManipulatorFactory,
            DomsEventStorage<T> domsEventStorage,
            int pageSize,
            String recordBase) throws MalformedURLException {
        super(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize);
        this.recordBase = recordBase;
    }

    @Override
    protected String toQueryString(Query<T> query) {
        final String queryWithoutRecordBase;
        if (query instanceof PassQThrough_Query) {
            queryWithoutRecordBase =  ((PassQThrough_Query) query).getQ();
        } else {
            // the recordBase is hardcoded in the super method so remove that and add our own recordBase
            String originalQuery = super.toQueryString(query);
            String originalQueryPrefix = spaced(RECORD_BASE); // from examining source
            if (originalQuery.startsWith(originalQueryPrefix)) {
                queryWithoutRecordBase = originalQuery.substring(originalQueryPrefix.length());
            } else {
                throw new RuntimeException(originalQuery + " does not start with '" + originalQueryPrefix + "'");
            }
        }
        final String finalQuery = spaced("recordBase:" + recordBase) + queryWithoutRecordBase;
        log.trace("q={}", finalQuery);
        return finalQuery;
    }
}
