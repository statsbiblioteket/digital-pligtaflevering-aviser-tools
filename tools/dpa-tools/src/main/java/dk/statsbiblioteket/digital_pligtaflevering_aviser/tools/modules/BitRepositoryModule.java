package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import org.apache.commons.codec.CharEncoding;

import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.function.Function;

import static dk.statsbiblioteket.medieplatform.autonomous.iterator.bitrepository.IngesterConfiguration.BITMAG_BASEURL_PROPERTY;

/**
 * Dagger module for bitrepository related dependencies.
 */
@Module
public class BitRepositoryModule {

    public static final String BITREPOSITORY_SBPILLAR_MOUNTPOINT = "bitrepository.sbpillar.mountpoint";
    public static final String PROVIDE_ENCODE_PUBLIC_URL_FOR_FILEID = "provide.encode.public.url.for.fileid";

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

    /** The SB Bitrepository pillar allows direct access to the files in the repository, typically
     * in the form of a NFS mount point from where the file path can be resolved.
     *
     * @param map configuration map
     * @return the configuration parameter provided (required)
     */
    @Provides
    @Named(BITREPOSITORY_SBPILLAR_MOUNTPOINT)
    String getPutfileDestinationPath(ConfigurationMap map) {
        return map.getRequired(BITREPOSITORY_SBPILLAR_MOUNTPOINT);
    }


    @Provides
    @Named(PROVIDE_ENCODE_PUBLIC_URL_FOR_FILEID)
    public Function<String, String> provideEncodePublicURLForFileID(@Named(BITMAG_BASEURL_PROPERTY) String bitmagUrl) {
        return fileID -> {
            String singleEncodedFileId = null;
            try {
                singleEncodedFileId = URLEncoder.encode(fileID, CharEncoding.UTF_8);
                String doubleEncodedFileId = URLEncoder.encode(singleEncodedFileId, CharEncoding.UTF_8);
                return bitmagUrl + doubleEncodedFileId;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
