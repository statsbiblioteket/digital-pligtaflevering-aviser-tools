package dk.statsbiblioteket.digital_pligtaflevering_aviser.tools.modules;

import java.util.stream.Stream;

/**
 *
 */
public class InfomediaSingleDayNewspaper {
    private String s;

    public InfomediaSingleDayNewspaper(String s) {
        this.s = s;
    }

    public Stream<InfomediaSinglePagePDF> getInfomediaSinglePagePDFStream() {
        return Stream.of(new InfomediaSinglePagePDF());
    }

    @Override
    public String toString() {
        return "InfomediaSingleDayNewspaper{" +
                "s='" + s + '\'' +
                '}';
    }
}
