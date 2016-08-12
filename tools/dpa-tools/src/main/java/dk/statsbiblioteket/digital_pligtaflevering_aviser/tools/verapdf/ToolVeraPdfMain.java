package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.verapdf;

import java.io.FileReader;
import java.util.Collections;

/**
 *
 */
public class ToolVeraPdfMain {
    public static void main(String[] args) throws Exception {
        ConfigurationMap map = new ConfigurationMap();
        map.addPropertyFile(new FileReader(args[0]));

        ToolVeraPdfModule module = new ToolVeraPdfModule(Collections.unmodifiableMap(map));



    }
}
