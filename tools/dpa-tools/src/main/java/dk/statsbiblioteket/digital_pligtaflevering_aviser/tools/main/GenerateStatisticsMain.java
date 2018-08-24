package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.kb.stream.StreamTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsEvent;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.DomsItemTuple;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.convertersFunctions.JaxbUtils;
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
import static java.util.stream.Collectors.toList;

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
        Tool<StreamTuple<DomsItem, String>> getTool();
    }

    /**
     * @noinspection Convert2MethodRef
     */
    @Module
    protected static class GenerateStatisticsModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool<StreamTuple<DomsItem, String>> provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName, QuerySpecification query, DomsRepository domsRepository) {
            final String agent = GenerateStatisticsModule.class.getSimpleName();

            Tool<StreamTuple<DomsItem, String>> f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek((DomsItem deliveryItem) -> log.trace("Processing: {}", deliveryItem))
                    .map(DomsItemTuple::create)
                    .map(c -> c.map(v -> processChildDomsId().apply(v)))
                    .peek(c -> c.left().appendEvent(new DomsEvent(agent, new Date(), c.right().getHumanlyReadableMessage(), eventName, c.right().isSuccess())))
                    .map(c -> c.map(toolResult -> "outcome=" + toolResult.isSuccess()))
                    .peek((StreamTuple<DomsItem, String> o) -> log.trace("Processed: {}", o))
                    .collect(toList());
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
                DeliveryStatistics deliveryStatistics;
                try {
                    deliveryStatistics = parser.processDomsIdToStream().apply(domsItem);
                } catch (Exception e){
                    log.error("Caught Exception on item {}",domsItem,e);
                    return ToolResult.fail("Failed to generate statistics for delivery " + deliveryName+ " at the time " + new Date().toString());
                }
                byte[] statisticsStream;
                try {
                    statisticsStream = JaxbUtils.processDeliveryStatisticsToBytestream().apply(deliveryStatistics);
                } catch (Exception e){
                    log.error("Caught Exception on item {}",domsItem,e);
                    return ToolResult.fail("Failed marshall statistics as XML for " + deliveryName + " at the time " + new Date().toString());
                }

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

                return ToolResult.ok(new Date().toString());
            };
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}
