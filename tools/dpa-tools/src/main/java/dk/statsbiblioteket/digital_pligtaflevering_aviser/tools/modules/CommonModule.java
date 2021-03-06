package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ToolMXBean;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper.DPA_GIT_ID;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ToolMXBean.JMX_OBJECT_NAME;

/**
 * Module containing providers for typical DPA Dagger dependencies.
 */
@Module
public class CommonModule {

    /**
     * We want to be able to register the version of the software actually running in production. The shell script
     * generated to invoke autonomous components from a command line has the git branch and commit id as a system
     * property string assignment.  The string is constructed at assembly time.
     *
     * @param map configuration map
     * @return String describing the git commit id (if provided) otherwise default to "(non-production)"
     */
    @Produces
    @Provides
    @Named(DPA_GIT_ID)
    String provideGitId(ConfigurationMap map) {
        return map.getDefault(DPA_GIT_ID, "(non-production)");
    }

    /**
     * <p>
     * Return the ObjectName identifier for the JMX MXBean if required by the Tool.  A typical value is
     * <code>dk.kb.dpaviser:type=Ingester</code>.</p>
     * <p>See <a href="http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html?ssSourceSiteId=ocomen#mozTocId509360">
     * http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html?ssSourceSiteId=ocomen#mozTocId509360</a> for details.</p>
     *
     * @param map configuration map
     * @return the ObjectName string to use.
     */
    @Produces
    @Provides
    @Named(JMX_OBJECT_NAME)
    String provideJmxObjectName(ConfigurationMap map) {
        return map.getRequired(JMX_OBJECT_NAME);
    }

    /**
     * Return the event name to be used to register the outcome of this autonomous component on each DomsItem as indicated by
     * the work query
     *
     * @param map configuration map
     * @return the event name to store in DOMS for this item.
     */
    @Produces
    @Provides
    @Named(AUTONOMOUS_THIS_EVENT)
    String provideAutonomousThisEvent(ConfigurationMap map) {
        return map.getRequired(AUTONOMOUS_THIS_EVENT);
    }


    @Produces
    @Provides
    ToolMXBean provideToolMXBean(DefaultToolMXBean defaultToolMXBean) {
        return defaultToolMXBean;
    }
}
