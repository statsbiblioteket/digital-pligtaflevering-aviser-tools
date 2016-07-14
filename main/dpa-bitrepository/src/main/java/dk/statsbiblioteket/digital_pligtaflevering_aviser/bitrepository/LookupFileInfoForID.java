package dk.statsbiblioteket.digital_pligtaflevering_aviser.bitrepository;


import org.bitrepository.common.filestore.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LookupFileInfoForID implements Function<String, FileInfo> {

    static final AtomicInteger counter = new AtomicInteger();

    File[] FILES = {new File("/home/tra/Downloads/Git_at_SB.pdf"), new File("/home/tra/Downloads/NOM20150807X13#0001.pdf")};

    @Override
    public FileInfo apply(final String s) {
        final File f = FILES[counter.incrementAndGet() % FILES.length]; // round robin

        org.bitrepository.common.filestore.FileInfo fileInfo = new FileInfo() {
            @Override
            public String getFileID() {
                return s;
            }

            @Override
            public InputStream getInputstream() throws IOException {
                return new FileInputStream(f);
            }

            @Override
            public Long getMdate() {
                return f.lastModified();
            }

            @Override
            public long getSize() {
                return f.length();
            }
        };
        return fileInfo;
    }
}

