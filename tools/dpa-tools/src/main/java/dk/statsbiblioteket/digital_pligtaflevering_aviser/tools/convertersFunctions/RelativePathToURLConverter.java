package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Function;

/**
 *
 */
public class RelativePathToURLConverter implements Function<Path, URL> {
    private final String root;

    public RelativePathToURLConverter(String root) {
        this.root = root;
    }


    @Override
    public URL apply(Path relativePath)  {
        // http://www.baeldung.com/java-url-encoding-decoding
        try {
            URI uri = new URI("file", null, root + relativePath.toString(), null);
            return uri.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
