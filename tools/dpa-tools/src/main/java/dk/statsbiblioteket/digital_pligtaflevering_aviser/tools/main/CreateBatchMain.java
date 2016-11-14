package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.newspaper.CreateBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PASSWORD;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_PIDGENERATOR_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_URL;
import static dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants.DOMS_USERNAME;

/**
 * Unfinished create batch trigger main.
 */
public class CreateBatchMain {
    public static void main(String[] args) {
        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerCreateBatchMain_CreateBatchComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, CreateBatchModule.class})
    interface CreateBatchComponent {
        Tool getTool();
    }

    @Module
    static class CreateBatchModule {
        public static final String CREATEBATCH_BATCHID = "createbatch.batchid";
        public static final String CREATEBATCH_ROUNDTRIP = "createbatch.roundtrip";
        public static final String AUTONOMOUS_AGENT = "autonomous.agent";

        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(CREATEBATCH_BATCHID) String batchId,
                         @Named(CREATEBATCH_ROUNDTRIP) String roundTrip,
                         @Named(AUTONOMOUS_AGENT) String premisAgent,
                         @Named(DOMS_URL) String domsUrl,
                         @Named(DOMS_USERNAME) String domsUser,
                         @Named(DOMS_PASSWORD) String domsPass,
                         @Named(DOMS_PIDGENERATOR_URL) String urlToPidGen) {
            // FIXME:  Rewrite to watch folder.
            return () -> CreateBatch.main(new String[]{batchId, roundTrip, premisAgent, domsUrl, domsUser, domsPass, urlToPidGen});
        }

        @Provides
        @Named(CREATEBATCH_BATCHID)
        String provideBatchId(ConfigurationMap map) {
            return map.getRequired(CREATEBATCH_BATCHID);
        }

        @Provides
        @Named(CREATEBATCH_ROUNDTRIP)
        String provideRoundtrip(ConfigurationMap map) {
            return map.getRequired(CREATEBATCH_ROUNDTRIP);
        }

        @Provides
        @Named(AUTONOMOUS_AGENT)
        String provideAgent(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_AGENT);
        }
    }
}
