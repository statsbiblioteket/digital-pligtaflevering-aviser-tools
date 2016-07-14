package dk.statsbiblioteket.digital_pligtaflevering_aviser.bitrepository;

import org.bitrepository.common.filestore.FileInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/** Initial very dirty workaround implementation of the "Look this id up in the bit repository" functionality.
 * Just round robin between three small but broken PDF files inside the project.  As of 2016-07-14 the mock up
 * delivery from LLO does only contain two batches and no PDF-files so it is rather limited.
 */

public class LookupFileInfoForID implements Function<String, FileInfo> {

    static final AtomicInteger counter = new AtomicInteger();
    String[] RESOURCES = {
            "/veraPDF test suite 6-1-3-t01-pass-a.pdf",
            "/veraPDF test suite 6-1-10-t01-fail-c.pdf",
            "/veraPDF test suite 6-8-2-2-t01-fail-a.pdf"};

    @Override
    public FileInfo apply(final String s) {
        final String resource = RESOURCES[counter.incrementAndGet() % RESOURCES.length]; // round robin

        return new FileInfo() { // Fake a bit repository response.
            @Override
            public String getFileID() {
                return s;
            }

            @Override
            public InputStream getInputstream() throws IOException {
                return Objects.requireNonNull(getClass().getResourceAsStream(resource), resource);
            }

            @Override
            public Long getMdate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getSize() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

