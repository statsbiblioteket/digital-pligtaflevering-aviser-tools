package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.SBOIQuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Date;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 * List deletable deliveries for JHKL.  No events are set.</p>
 *
 * @noinspection WeakerAccess
 */
public class SetEventOnUuidMain {
    protected static final Logger log = LoggerFactory.getLogger(SetEventOnUuidMain.class);

    /**
     * Parameter to locate item in SBOI.  Give exact parameter to query for as <code>item_uuid:...</code> to SBOI.
     * Note:  To avoid accidents this must match one single item.
     */
    public static final String ITEM_UUID = "item_uuid";
    public static final String EVENT_TO_ADD = "event";
    public static final String EVENT_MESSAGE = "message";
    public static final String OUTCOME = "outcome";

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerSetEventOnUuidMain_SetEventOnUuidComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, SetEventOnUuidModule.class})
    interface SetEventOnUuidComponent {
        Tool<String> getTool();
    }

    /**
     * @noinspection WeakerAccess
     */
    @Module
    protected static class SetEventOnUuidModule {
        Logger log = LoggerFactory.getLogger(SetEventOnUuidMain.class);  // short name

        /**
         * @noinspection PointlessBooleanExpression, UnnecessaryLocalVariable, unchecked
         */
        @Provides
        Tool<String> provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                                        @Named(ITEM_UUID) String item_uuid,
                                                        @Named(EVENT_TO_ADD) String eventToAdd,
                                                        @Named(EVENT_MESSAGE) String eventMessage,
                                                        @Named(OUTCOME) boolean outcome,

                                                        DomsRepository domsRepository,
                                                        DefaultToolMXBean mxBean) {
            final String agent = SetEventOnUuidMain.class.getSimpleName();

            QuerySpecification workToDoQuery = new SBOIQuerySpecification(" and +" + ITEM_UUID + ":" + SBOIQuerySpecification.quote(item_uuid));

            long count = domsRepository.count(workToDoQuery);

            if (count > 1) {
                throw new RuntimeException("Matches " + count + " items.  Please work on a single item at a time!");
            }

            //
            Tool<String> tool = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(item -> log.info("Got : {}", item))
                    .peek(item -> mxBean.currentId = item.getPath() + " " + item.getDomsId().id())
                    .peek(item -> mxBean.idsProcessed++)

                    .peek(item -> item.appendEvent(new DomsEvent(agent, new Date(), eventMessage, eventToAdd, outcome)))
                    .map(item -> item.getPath() + " " + item.getDomsId().id())
                    .collect(toList());

            return tool;
        }

        @Provides
        @Named(ITEM_UUID)
        String provideItemUuid(ConfigurationMap map) {
            return map.getRequired(ITEM_UUID);
        }
        @Provides
        @Named(EVENT_TO_ADD)
        String provideEventToAdd(ConfigurationMap map) {
            return map.getRequired(EVENT_TO_ADD);
        }
        @Provides
        @Named(EVENT_MESSAGE)
        String provideEventMessage(ConfigurationMap map) {
            return map.getRequired(EVENT_MESSAGE);
        }

        @Provides
        @Named(OUTCOME)
        boolean provideOutcome(ConfigurationMap map) {
            return Boolean.valueOf(map.getRequired(OUTCOME));
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}

