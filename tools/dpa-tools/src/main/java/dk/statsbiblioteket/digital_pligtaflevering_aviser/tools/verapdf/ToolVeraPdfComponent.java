package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.verapdf;

import dagger.Component;

/**
 *
 */
@Component(modules = ToolVeraPdfModule.class)
public interface ToolVeraPdfComponent {
    ToolMaker maker();
}
