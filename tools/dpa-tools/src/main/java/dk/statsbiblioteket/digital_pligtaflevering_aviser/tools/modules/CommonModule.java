package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;

import javax.inject.Named;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper.DPA_GIT_ID;

/**
 * Module containing providers for typical DPA Dagger dependencies.
 */
@Module
public class CommonModule {

    /** We want to be able to register the version of the software actually running in production.
     * The shell script generated to invoke autonomous components from a command line
     * has the git branch and commit id as a system property string assignment.  The
     * string is constructed at assembly time.
     * @param map
     * @return
     */
    @Provides
    @Named(DPA_GIT_ID) String provideGitId(ConfigurationMap map) {
        return map.getDefault(DPA_GIT_ID, "(non-production)");
    }
}
