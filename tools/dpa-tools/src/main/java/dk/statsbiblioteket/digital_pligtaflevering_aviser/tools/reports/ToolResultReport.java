package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.reports;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class ToolResultReport implements Function<Stream<ToolResult>, String> {
    @Override
    public String apply(Stream<ToolResult> toolResultStream) {
        Map<Boolean, List<ToolResult>> m = toolResultStream.collect(Collectors.groupingBy(tr -> tr.isSuccess()));

        List<ToolResult> ok = m.getOrDefault(Boolean.TRUE, Collections.emptyList());
        List<ToolResult> failed = m.getOrDefault(Boolean.FALSE, Collections.emptyList());

        if (failed.size() == 0) {
            return ok.size() + " processed.";
        }

        StringBuilder sb = new StringBuilder("" + ok.size() + " processed.\n\n");
        sb.append(failed.size() + " failed!\n\n--------------\n");
        for (ToolResult f:failed) {
            sb.append(f.getItem() + ": " + f.getHumanlyReadableMessage() + "\n");
        }
        sb.append("\n--------------\n");
        return sb.toString();
    }
}
