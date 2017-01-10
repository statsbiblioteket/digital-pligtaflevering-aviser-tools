package dk.statsbiblioteket.digital_pligtaflevering_aviser.web.mock;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SampleData {
    public static List<SampleData> successData =
            Arrays.asList(new SampleData("dl_20160104_rt1", new SampleEvent("1"),new SampleEvent("2"), new SampleEvent("3")),
                    new SampleData("dl_20160102_rt1", new SampleEvent("4"), new SampleEvent("5"), new SampleEvent("6")),
                    new SampleData("dl_20160103_rt1", new SampleEvent("7")),
                    new SampleData("dl_20160101_rt1", new SampleEvent("8"), new SampleEvent("9")));

    public String getDelivery() {
        return delivery;
    }

    protected String delivery;

    public SampleEvent[] getEvents() {
        return events;
    }

    private SampleEvent[] events;

    @Override
    public String toString() {
        return "SampleData{" +
                "delivery='" + delivery + '\'' +
                ", events=" + Arrays.toString(events) +
                '}';
    }

    public SampleData(String delivery, SampleEvent... events) {

        this.delivery = delivery;
        this.events = events;
    }

    public static class SampleEvent {
        private String s;

        public SampleEvent(String s) {
            this.s = s;
        }

        public String getS() {
            return s;
        }

        @Override
        public String toString() {
            return "SampleEvent{" +
                    "s='" + s + '\'' +
                    '}';
        }
    }
}
