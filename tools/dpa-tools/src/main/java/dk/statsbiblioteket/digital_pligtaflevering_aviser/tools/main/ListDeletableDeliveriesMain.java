package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
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
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;
import static java.util.stream.Collectors.toList;

/**
 * <p>
 * List deletable deliveries for JHKL.  No events are set.</p>
 *
 * @noinspection WeakerAccess
 */
public class ListDeletableDeliveriesMain {
    protected static final Logger log = LoggerFactory.getLogger(ListDeletableDeliveriesMain.class);

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerListDeletableDeliveriesMain_ListDeletableDeliveriesComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, ListDeletableDeliveriesModule.class})
    interface ListDeletableDeliveriesComponent {
        Tool<String> getTool();
    }

    /**
     * @noinspection WeakerAccess, Convert2MethodRef
     */
    @Module
    protected static class ListDeletableDeliveriesModule {
        Logger log = LoggerFactory.getLogger(ListDeletableDeliveriesMain.class);  // short name

        /**
         * @noinspection PointlessBooleanExpression, UnnecessaryLocalVariable, unchecked
         */
        @Provides
        Tool<String> provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                                                        QuerySpecification workToDoQuery,
                                                        DomsRepository domsRepository,
                                                        DefaultToolMXBean mxBean) {
            final String agent = ListDeletableDeliveriesMain.class.getSimpleName();

            //
            Tool<String> tool = () -> Stream.of(workToDoQuery)
                    .flatMap(domsRepository::query)
                    .peek(roundtripItem -> log.info("Got : {}", roundtripItem))
                    .peek(roundtripItem -> mxBean.currentId = roundtripItem.getPath() + " " + roundtripItem.getDomsId().id())
                    .peek(roundtripItem -> mxBean.idsProcessed++)

                    // We do not need to do much.  Just create a tab-separated string of values and print it to standard out.
                    .map(roundtripItem -> String.join("\t", roundtripItem.getPath(), roundtripItem.getDomsId().id()))
                    .peek((String s) -> System.out.println(s))
                    .collect(toList());

            return tool;
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}

