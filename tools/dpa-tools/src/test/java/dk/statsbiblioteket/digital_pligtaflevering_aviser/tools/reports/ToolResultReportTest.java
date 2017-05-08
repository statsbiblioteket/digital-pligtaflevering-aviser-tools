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
    public void test1(){
        ToolResultReport trr = new ToolResultReport();

        final ToolResult ok1 = ToolResult.ok(null, "Yes!");
        final ToolResult ok2 = ToolResult.ok(null, "Well!");
        final ToolResult ok3 = ToolResult.ok(null, "Oh!");
        final ToolResult fail1 = ToolResult.fail(null, "No!");
        final ToolResult fail2 = ToolResult.fail(null, "Zip!");

        assertEquals("1 processed.", trr.apply(Stream.of(ok1)));
        assertEquals("2 processed.", trr.apply(Stream.of(ok1, ok2)));
        assertEquals("3 processed.", trr.apply(Stream.of(ok1, ok2, ok3)));

        assertEquals("1 processed.\n\n" +
                "1 failed!\n" +
                "\n" +
                "--------------\n" +
                "null: No!\n" +
                "\n" +
                "--------------\n", trr.apply(Stream.of(ok1, fail1)));
        assertEquals("2 processed.\n" +
                "\n" +
                "1 failed!\n" +
                "\n" +
                "--------------\n" +
                "null: Zip!\n" +
                "\n" +
                "--------------\n", trr.apply(Stream.of(ok1, ok2, fail2)));
        assertEquals("3 processed.\n" +
                "\n" +
                "2 failed!\n" +
                "\n" +
                "--------------\n" +
                "null: No!\n" +
                "null: Zip!\n" +
                "\n" +
                "--------------\n", trr.apply(Stream.of(ok1, ok2, ok3, fail1, fail2)));
    }
}
