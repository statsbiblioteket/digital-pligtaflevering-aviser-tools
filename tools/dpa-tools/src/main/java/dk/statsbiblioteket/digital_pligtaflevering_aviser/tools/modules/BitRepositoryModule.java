package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.inject.Named;

import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * Dagger module for bitrepository related dependencies.
 */
@Module
public class BitRepositoryModule {

    public static final String BITREPOSITORY_SBPILLAR_MOUNTPOINT = "bitrepository.sbpillar.mountpoint";

    /** This is the base url for the public exposure of a given file ingested in the BitRepository.  This
     * is stored as the URL of the CONTENTS datastream.
     * For the SB pillar we can remove this prefix from the URL and get the actual relative filename on disk.
     * Typically this is stored
     *
     * @param map configuration map
     * @return the configuration parameter provided (required)
     */
    @Provides
    public @Named(BITMAG_BASEURL_PROPERTY) String provideBitrepositoryBaseUrl(ConfigurationMap map) {
        return map.getRequired(BITMAG_BASEURL_PROPERTY);
    }

    @Provides
    @Named(BITREPOSITORY_SBPILLAR_MOUNTPOINT)
    String getPutfileDestinationPath(ConfigurationMap map) {
        return map.getRequired(BITREPOSITORY_SBPILLAR_MOUNTPOINT);
    }


}
