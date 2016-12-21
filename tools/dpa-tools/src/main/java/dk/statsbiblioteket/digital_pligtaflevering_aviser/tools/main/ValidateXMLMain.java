package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.main;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsDatastream;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsId;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsItem;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.DomsRepository;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.QuerySpecification;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.AutonomousPreservationToolHelper;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.ConfigurationMap;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.model.ToolResult;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class for starting autonomous component
 * This component is used for validation of metadata from newspaper deliveries.
 * The metadata is ingested into fedora-commons and is now validated against *.xsd and defined rules
 */
public class ValidateXMLMain {
    protected static final Logger log = LoggerFactory.getLogger(ValidateXMLMain.class);

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerValidateXMLMain_ValidateXMLComponent.builder().configurationMap(m).build().getTool()
        );



    }



    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, IngesterModule.class})
    interface ValidateXMLComponent {
        Tool getTool();
    }

    @Module
    protected static class IngesterModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(QuerySpecification query, DomsRepository domsRepository) {
            Tool f = () -> Stream.of(query)
                    .flatMap(domsRepository::query)
                    .peek(o -> log.trace("Query returned: {}", o))
                    .map(domsId -> processChildDomsId(domsRepository).apply(domsId))
                    .collect(Collectors.toList())
                    .toString();

            return f;
        };


        private Function<DomsId, String> processChildDomsId(DomsRepository domsRepository) {
            return domsId -> {
                long startTime = System.currentTimeMillis();

                // Single doms item
                List<ToolResult> toolResults = domsRepository.allChildrenFor(domsId).stream()
                        .flatMap(childDomsId -> analyzeXML(childDomsId, domsRepository))
                        .collect(Collectors.toList());

                // Sort according to result
                final Map<Boolean, List<ToolResult>> toolResultMap = toolResults.stream()
                        .collect(Collectors.groupingBy(tr -> tr.getResult()));

                List<ToolResult> failingToolResults = toolResultMap.getOrDefault(Boolean.FALSE, Collections.emptyList());

                String deliveryEventMessage = failingToolResults.stream()
                        .map(tr -> "---\n" + tr.getHumanlyReadableMessage() + "\n" + tr.getHumanlyReadableStackTrace())
                        .filter(s -> s.trim().length() > 0) // skip blank lines
                        .collect(Collectors.joining("\n"));

                // outcome was successful only if no toolResults has a FALSE result.
                boolean outcome = failingToolResults.size() == 0;

                final String keyword = getClass().getSimpleName();
                final Date timestamp = new Date();

                //domsRepository.appendEventToItem(domsId, keyword, timestamp, deliveryEventMessage, VERAPDF_RUN, outcome);

                log.info("{} {} Took: {} ms", keyword, domsId, (System.currentTimeMillis() - startTime));
                return "domsID " + domsId + " processed. " + failingToolResults.size() + " failed. outcome = " + outcome;
            };
        }





        protected Stream<ToolResult> analyzeXML(DomsId domsId, DomsRepository domsRepository) {
            DomsItem domsItem = domsRepository.lookup(domsId);

            final List<DomsDatastream> datastreams = domsItem.datastreams();

            Optional<DomsDatastream> profileOptional = datastreams.stream()
                    .filter(ds -> ds.getID().equals("XML"))
                    .findAny();

            if (profileOptional.isPresent() == false) {
                return Stream.of();
            }
            //log.trace("analyzePDF found PDF datastream on {}", domsId);


            DomsDatastream ds = profileOptional.get();

            System.out.println("------------------------------------------------> ");


            for(DomsDatastream stream : datastreams) {
                System.out.println(stream.getID());
            }

            return Stream.of(ToolResult.ok("id: " + domsId + " " + "comment"));
        }



        @Provides
        ItemFactory<Item> provideItemFactory() {
            return id -> new Item();
        }


        @Provides
        @Named("pageSize")
        Integer providePageSize(ConfigurationMap map) {
            return Integer.valueOf(map.getRequired("pageSize"));
        }

    }
}
