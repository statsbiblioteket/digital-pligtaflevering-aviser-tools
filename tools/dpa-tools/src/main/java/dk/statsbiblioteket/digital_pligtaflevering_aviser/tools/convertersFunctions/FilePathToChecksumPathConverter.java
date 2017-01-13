package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions;

import java.nio.file.Path;
import java.util.function.BiFunction;

/**
 * Converter for convertion of a Path into something which is matching the path in the checksumfile
 */
public interface FilePathToChecksumPathConverter extends BiFunction<Path, String, String> {
}
