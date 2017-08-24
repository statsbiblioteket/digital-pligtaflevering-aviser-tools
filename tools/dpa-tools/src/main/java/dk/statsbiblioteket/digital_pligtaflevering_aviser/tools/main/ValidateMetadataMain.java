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
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.DefaultToolMXBean;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.harness.Tool;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.CommonModule;
import dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules.DomsModule;
import dk.statsbiblioteket.medieplatform.autonomous.Item;
import dk.statsbiblioteket.medieplatform.autonomous.ItemFactory;
import javaslang.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main class for starting autonomous component
 * This component is used for validation of metadata from newspaper deliveries.
 * The metadata is ingested into fedora-commons and is now validated against *.xsd and defined rules
 */

public class ValidateMetadataMain {
    protected static final Logger log = LoggerFactory.getLogger(ValidateMetadataMain.class);

    public static final String AUTONOMOUS_THIS_EVENT = "autonomous.thisEvent";

    public static void main(String[] args) {

        AutonomousPreservationToolHelper.execute(
                args,
                m -> DaggerValidateXMLMain_ValidateXMLComponent.builder().configurationMap(m).build().getTool()
        );
    }

    @Component(modules = {ConfigurationMap.class, CommonModule.class, DomsModule.class, ValidateMetadataModule.class})
    interface ValidateMetadataComponent {
        Tool getTool();
    }

    @Module
    protected static class ValidateMetadataModule {
        Logger log = LoggerFactory.getLogger(this.getClass());

        @Provides
        Tool provideTool(@Named(AUTONOMOUS_THIS_EVENT) String eventName,
                         QuerySpecification workToDoQuery,
                         DomsRepository domsRepository,
                         Function<DomsItem, ToolResult> processDelivery,
                         DefaultToolMXBean mxBean) {
            Tool f = () -> {
                return Stream.of(workToDoQuery)
                        .flatMap(domsRepository::query)
                        .peek(domsItem -> log.trace("Processing delivery:{}", domsItem))
                        .map(processDelivery)
                        .collect(Collectors.toList())
                        .toString();

            };
            return f;
        }

        @Provides
        Function<DomsItem, ToolResult> provideDeliveryProcessor(
                Function<DomsItem, Stream<Try<ToolResult>>> processDeliveryChild,
                Function<DomsItem, Collector<Try<ToolResult>, OkFailThrown, ToolResult>> toolResultCollectorFunction) {
            return domsItem -> domsItem.allChildren()
                    .flatMap(processDeliveryChild)
                    .collect(toolResultCollectorFunction.apply(domsItem));
        }

        @Provides
        Function<DomsItem, Stream<Try<ToolResult>>> provideDeliveryChildProcessor() {
            throw new UnsupportedOperationException();
        }

        static class OkFailThrown {
            public OkFailThrown(List<ToolResult> ok, List<ToolResult> fail, List<Try<ToolResult>> thrown) {
                this.ok = Objects.requireNonNull(ok, "ok");
                this.fail = Objects.requireNonNull(fail, "fail");
                this.thrown = Objects.requireNonNull(thrown, "thrown");
            }

            final List<ToolResult> ok, fail;
            final List<Try<ToolResult>> thrown;
        }

        @Provides
        Function<DomsItem, Collector<Try<ToolResult>, OkFailThrown, ToolResult>> provideToolResultCollector() {
            return domsItem -> {
                Supplier<OkFailThrown> supplier = () -> new OkFailThrown(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                BiConsumer<OkFailThrown, Try<ToolResult>> accumulator = (c, t) -> {
                    if (t.isFailure()) {
                        c.thrown.add(t);
                    } else if (t.get().isSuccess()) {
                        c.ok.add(t.get());
                    } else {
                        c.fail.add(t.get());
                    }
                };
                BinaryOperator<OkFailThrown> combiner = (c1, c2) -> new OkFailThrown(concat(c1.ok, c2.ok), concat(c1.fail, c2.fail), concat(c1.thrown, c2.thrown));
                Function<OkFailThrown, ToolResult> finisher = (c) -> {
                    if (c.fail.size() == 0 && c.thrown.size() == 0) { // all well.
                        return ToolResult.ok(domsItem, c.ok.size() + " processed.");
                    } else {

                        String message1 = c.thrown.size() > 0
                                ? c.thrown.stream()
                                .flatMap(t -> stacktraceFor(t.getCause())).collect(Collectors.joining("\n\n"))
                                : "";

                        String message2 = c.fail.size() > 0
                                ? c.fail.stream()
                                .flatMap(tr -> Stream.of("---", tr.getItem() + ": " + tr.getHumanlyReadableMessage(), ""))
                                .filter(s -> s.trim().length() > 0) // skip blank lines
                                .collect(Collectors.joining("\n"))
                                : "";

                        return ToolResult.fail(domsItem, message1 + "\n" + message2);
                    }
                };
                return Collector.of(supplier, accumulator, combiner, finisher);
            };
        }

        private Stream<String> stacktraceFor(Throwable cause) {
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            final String[] lines = sw.toString().split("\n");
            return Arrays.stream(lines);
        }

        private <T> List<T> concat(List<T> l1, List<T> l2) {
            List<T> l = new ArrayList<>();
            l.addAll(l1);
            l.addAll(l2);
            return l;
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
