package dk.statsbiblioteket.medieplatform.autonomous;

import java.net.MalformedURLException;

/**
 * This class is a work around that the record base needed in the summa query string is currently hardcoded in
 * SBOIEventIndex in a dependent artifact.  A Provider is necessary as the constructor can throw checked
 * exceptions.
 */
public class SBOIEventIndex_RecordBaseAsParameter<T extends Item> extends SBOIEventIndex<T> {
    private final String recordBase;

    public SBOIEventIndex_RecordBaseAsParameter(
            String summaLocation,
            PremisManipulatorFactory<T> premisManipulatorFactory,
            DomsEventStorage<T> domsEventStorage,
            int pageSize,
            String recordBase) throws MalformedURLException {
        super(summaLocation, premisManipulatorFactory, domsEventStorage, pageSize);
        this.recordBase = recordBase;
    }

    @Override
    protected String toQueryString(Query query) {
        String originalQuery = super.toQueryString(query);
        String originalQueryPrefix = spaced(RECORD_BASE); // from examining source
        String strippedQuery = originalQuery.substring(originalQueryPrefix.length());
        String newQuery = spaced("recordBase:" + recordBase) + strippedQuery;
        return newQuery;
    }
}