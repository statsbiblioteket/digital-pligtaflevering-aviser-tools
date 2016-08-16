package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.verapdf;

import dagger.Component;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.MainFunction;

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

    @Component(modules = ToolVeraPdfModule.class)
    public static interface ToolVeraPdfMainComponent extends MainFunction {

    }
}
