package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

import org.mockito.Matchers;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperDomsEventStorage;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CreateBatchTest {

    /**
     * Test normal case: RT1 received, no other roundtrips exist.
     * Note: The batch is not known by DOMS, only MFPAK, so the getAllRoundTrips should return null.
     * Expected behaviour: RT1 has an event added.
     */
    @Test
    public void testDoWorkRT1() throws Exception {
        Batch batch1 = new Batch("1234", 1);
        batch1.setEventList(Collections.<Event>emptyList());

        NewspaperDomsEventStorage domsStorage = mock(NewspaperDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(null);

        CreateBatch.doWork(new Batch("1234", 1), "premisAgent", domsStorage, new Date());
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Batch("1234", 1)), eq("premisAgent"), Matchers.<Date>any(),
                                                      anyString(), eq("Data_Received"), eq(true));
        verifyNoMoreInteractions(domsStorage);
    }

    /**
     * Test normal case: RT2 received, RT1 already exists.
     * Expected behaviour: RT2 has an event added. RT1 gets a stop event.
     */
    @Test
    public void testDoWorkRT2() throws Exception {
        Batch batch1 = new Batch("1234", 1);
        batch1.setEventList(Collections.<Event>emptyList());
        Batch batch2 = new Batch("1234", 2);
        batch2.setEventList(Collections.<Event>emptyList());

        NewspaperDomsEventStorage domsStorage = mock(NewspaperDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(batch1));

        CreateBatch.doWork(new Batch("1234", 2), "premisAgent", domsStorage, new Date());
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Batch("1234", 2)), eq("premisAgent"), Matchers.<Date>any(),
                                                      anyString(), eq("Data_Received"), eq(true));
        verify(domsStorage, times(1)).appendEventToItem(eq(new Batch("1234", 1)), eq("premisAgent"), Matchers.<Date>any(), contains("Newer roundtrip (2) has been received, so this batch should be stopped"), eq("Manually_stopped"), eq(true));
        verifyNoMoreInteractions(domsStorage);
    }

    /**
     * Test exceptional case: RT1 received, RT2 already exists.
     * Expected behaviour: RT1 gets a failed event added.
     */
    @Test
    public void testDoWorkRT1afterRT2() throws Exception {
        Batch batch1 = new Batch("1234", 1);
        batch1.setEventList(Collections.<Event>emptyList());
        Batch batch2 = new Batch("1234", 2);
        batch2.setEventList(Collections.<Event>emptyList());

        NewspaperDomsEventStorage domsStorage = mock(NewspaperDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(batch2));

        CreateBatch.doWork(new Batch("1234", 1), "premisAgent", domsStorage, new Date());
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Batch("1234", 1)), eq("premisAgent"), Matchers.<Date>any(),
                                                      contains("Roundtrip (2) is newer than this roundtrip (1)"), eq("Data_Received"), eq(false));
        verifyNoMoreInteractions(domsStorage);
    }

    /**
     * Test exceptional case: RT2 received, RT1 already approved.
     * Expected behaviour: RT2 gets a failed event added.
     */
    @Test
    public void testDoWorkRT2whereRT1Approved() throws Exception {
        Batch batch1 = new Batch("1234", 1);
        Event event = new Event();
        event.setEventID("Roundtrip_Approved");
        event.setSuccess(true);
        batch1.setEventList(Arrays.asList(event));
        Batch batch2 = new Batch("1234", 2);
        batch2.setEventList(Collections.<Event>emptyList());

        NewspaperDomsEventStorage domsStorage = mock(NewspaperDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(batch1));

        CreateBatch.doWork(new Batch("1234", 2), "premisAgent", domsStorage, new Date());
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Batch("1234", 2)), eq("premisAgent"), Matchers.<Date>any(),
                                                      contains("Roundtrip (1) is already approved, so this roundtrip (2)"), eq("Data_Received"), eq(true));
        verify(domsStorage, times(1)).appendEventToItem(eq(new Batch("1234", 2)), eq("premisAgent"), Matchers.<Date>any(),
                contains("Another Roundtrip is already approved, so this batch should be stopped"), eq("Manually_stopped"), eq(true));
        verifyNoMoreInteractions(domsStorage);
    }
}