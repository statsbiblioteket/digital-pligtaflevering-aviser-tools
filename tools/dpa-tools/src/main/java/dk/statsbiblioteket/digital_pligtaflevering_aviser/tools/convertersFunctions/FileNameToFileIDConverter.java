package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import java.nio.file.Path;
import java.util.function.Function;

/**
 * Converter for converting Path into a fileId to be used by bitMagasin
 */
public interface FileNameToFileIDConverter extends Function<Path, String> {
}
