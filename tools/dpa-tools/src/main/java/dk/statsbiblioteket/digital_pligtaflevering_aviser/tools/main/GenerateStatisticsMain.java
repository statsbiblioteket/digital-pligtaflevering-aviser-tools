package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsValue;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.ingester.KibanaLoggingStrings;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsParser;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Stream;

import static dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool.AUTONOMOUS_THIS_EVENT;

/**
 * Main class for starting autonomous component This component is used for validation of metadata from newspaper
 * deliveries. The metadata is ingested into fedora-commons and is now validated against *.xsd and defined rules
 */
public class GenerateStatisticsMain {
    protected static final Logger log = LoggerFactory.getLogger(GenerateStatisticsMain.class);

    public static final String STATISTICS_STREAM_NAME = "DELIVERYSTATISTICS";

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerGenerateStatisticsMain_GenerateStatisticsComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, GenerateStatisticsModule.class})
    interface GenerateStatisticsComponent {
        Tool getTool();
    }

    /**
     * @noinspection Convert2MethodRef
     */
    @Module
    protected static class GenerateStatisticsModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName, QuerySpecification query, DomsRepository domsRepository) {
            final String agent = GenerateStatisticsModule.class.getSimpleName();

            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(domsItem -> log.trace("Processing: {}", domsItem))
                    .map(DomsValue::create)
                    .map(c -> c.map(v -> processChildDomsId().apply(v)))
                    .peek(c -> c.left().appendEvent(new DomsEvent(agent, new Date(), c.right().getHumanlyReadableMessage(), eventName, c.right().isSuccess())))
                    .count() + " items processed";

            return f;
        }

        /**
         * Generate statistics from the content of all child-elements
         *
         * @return
         */
        private Function<DomsItem, ToolResult> processChildDomsId() {
            return domsItem -> {
                DomsParser parser = new DomsParser();
                String deliveryName = domsItem.getPath();
                long startDeliveryStatTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.START_GENERATE_STATISTICS, deliveryName);
                DeliveryStatistics deliveryStatistics = parser.processDomsIdToStream().apply(domsItem);
                if (deliveryStatistics == null) {
                    return ToolResult.fail("The statistics which should be generated from the delivery could not be generated");
                }
                byte[] statisticsStream = parser.processDeliveryStatisticsToBytestream().apply(deliveryStatistics);
                String settingDate = new java.util.Date().toString();

                domsItem.modifyDatastreamByValue(
                        STATISTICS_STREAM_NAME,
                        null, // no checksum
                        null, // no checksum
                        statisticsStream,
                        null,
                        "text/xml",
                        null,
                        null);

                long finishedDeliveryStatTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.FINISHED_GENERATE_STATISTICS, deliveryName, finishedDeliveryStatTime - startDeliveryStatTime);

                if (statisticsStream != null) {
                    return ToolResult.ok(settingDate);
                } else {
                    return ToolResult.fail("Statistics could not get generated on " + deliveryName + " at the time " + settingDate);
                }
            };
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}
