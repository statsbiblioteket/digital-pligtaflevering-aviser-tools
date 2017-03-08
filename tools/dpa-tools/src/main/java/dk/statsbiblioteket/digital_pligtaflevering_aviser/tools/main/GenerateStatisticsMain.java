package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.statistics.DeliveryStatistics;
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


/**
 * Main class for starting autonomous component
 * This component is used for validation of metadata from newspaper deliveries.
 * The metadata is ingested into fedora-commons and is now validated against *.xsd and defined rules
 */
public class GenerateStatisticsMain {
    protected static final Logger log = LoggerFactory.getLogger(GenerateStatisticsMain.class);

    public static final String AUTONOMOUS_THIS_EVENT = "autonomous.thisEvent";
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
                    .map(domsItem -> processChildDomsId().apply(domsItem))
                    .peek(tr -> tr.getItem().appendEvent(agent, new Date(), tr.getHumanlyReadableMessage(), eventName, tr.getResult()))
                    .count() + " items processed";

            return f;
        }

        /**
         * Validate all xml-contents located as child under the delivery
         *
         * @return
         */
        private Function<DomsItem, ToolResult> processChildDomsId() {
            return domsItem -> {
                DomsParser parser = new DomsParser();
                String deliveryName = domsItem.getPath();
                long startDeliveryIngestTime = System.currentTimeMillis();
                log.info(KibanaLoggingStrings.START_DELIVERY_XML_VALIDATION_AGAINST_XSD, deliveryName);
                DeliveryStatistics deliveryStatistics = parser.processDomsIdToStream().apply(domsItem);
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

                if (statisticsStream != null) {
                    return ToolResult.ok(domsItem, settingDate);
                } else {
                    return ToolResult.fail(domsItem, settingDate);
                }
            };
        }

        /**
         * Provide the parameter to be written as sucessfull when the component has finished
         *
         * @param map
         * @return
         */
        @Provides
        @Named(AUTONOMOUS_THIS_EVENT)
        String thisEventName(ConfigurationMap map) {
            return map.getRequired(AUTONOMOUS_THIS_EVENT);
        }

        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }
    }
}
