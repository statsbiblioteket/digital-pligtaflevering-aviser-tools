package dk.statsbiblioteket.digital_pligtaflevering_aviser.doms;

import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Item;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 */
@Deprecated
public class DomsEventAdderCollection extends ArrayList<DomsEvent> {

    private DomsId domsId;
    private DomsEventStorage<Item> domsEventStorage;

    public DomsEventAdderCollection(DomsId domsId, DomsEventStorage<Item> domsEventStorage) {
        this.domsId = domsId;
        this.domsEventStorage = domsEventStorage;
    }

    @Override
    public boolean add(DomsEvent event) {
        try {
            Date timestamp = new Date();

            Item fakeItemToGetThroughAPI = new Item(domsId.id());

            Date d = domsEventStorage.appendEventToItem(
                    fakeItemToGetThroughAPI,
                    event.getLinkingAgentIdentifierValue(),
                    timestamp,
                    event.getEventOutcomeDetailNote(),
                    event.getEventType(),
                    event.getOutcome());
            System.out.println(d); // FIXME:  Do we need the date?
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
