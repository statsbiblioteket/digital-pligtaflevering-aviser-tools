package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.verapdf;

import dagger.Module;

import java.util.Map;

/**
 *
 */
@Module
public class ToolVeraPdfModule {
    private final Map<String, String> map;

    public ToolVeraPdfModule(Map<String, String> map) {
        this.map = map;
    }


}
