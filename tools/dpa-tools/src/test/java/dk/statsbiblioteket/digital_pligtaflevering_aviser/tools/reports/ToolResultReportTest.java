package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.reports;

import dk.statsbiblioteket.digital_pligtaflevering_aviser.doms.ToolResult;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ToolResultReportTest {

    @Test
    public void test1() {
        ToolResultReport trr = new ToolResultReport();

        final ToolResult ok1 = ToolResult.ok("Yes!");
        final ToolResult ok2 = ToolResult.ok("Well!");
        final ToolResult ok3 = ToolResult.ok("Oh!");
        final ToolResult fail1 = ToolResult.fail("No!");
        final ToolResult fail2 = ToolResult.fail("Zip!");

        assertEquals("1 processed.", trr.apply(Stream.of(ok1)));
        assertEquals("2 processed.", trr.apply(Stream.of(ok1, ok2)));
        assertEquals("3 processed.", trr.apply(Stream.of(ok1, ok2, ok3)));

        assertEquals("1 processed.\n\n" +
                "1 failed!\n" +
                "\n" +
                "--------------\n" +
                "FIXME: No!\n" +
                "\n" +
                "--------------\n", trr.apply(Stream.of(ok1, fail1)));
        assertEquals("2 processed.\n" +
                "\n" +
                "1 failed!\n" +
                "\n" +
                "--------------\n" +
                "FIXME: Zip!\n" +
                "\n" +
                "--------------\n", trr.apply(Stream.of(ok1, ok2, fail2)));
        assertEquals("3 processed.\n" +
                "\n" +
                "2 failed!\n" +
                "\n" +
                "--------------\n" +
                "FIXME: No!\n" +
                "FIXME: Zip!\n" +
                "\n" +
                "--------------\n", trr.apply(Stream.of(ok1, ok2, ok3, fail1, fail2)));
    }
}
