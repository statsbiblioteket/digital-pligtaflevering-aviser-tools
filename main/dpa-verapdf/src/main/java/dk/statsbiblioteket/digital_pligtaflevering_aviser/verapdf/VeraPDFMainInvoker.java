package dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf;

import org.verapdf.cli.VeraPdfCli;

/**
 *
 */
public class VeraPDFMainInvoker { // implements Function<InputStream, {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        VeraPdfCli.main(new String[]{"-r", "/home/tra/verapdf/corpus/veraPDF-corpus-staging"});
        VeraPdfCli.main(new String[]{"-r", "/home/tra/verapdf/corpus/veraPDF-corpus-staging"});
        VeraPdfCli.main(new String[]{"-r", "/home/tra/verapdf/corpus/veraPDF-corpus-staging"});
        System.err.println(">>" + (System.currentTimeMillis() - start));
    }
}
