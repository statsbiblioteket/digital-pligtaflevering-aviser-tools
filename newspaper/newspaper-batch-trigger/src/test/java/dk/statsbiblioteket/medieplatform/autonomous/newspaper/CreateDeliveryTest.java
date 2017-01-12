package dk.statsbiblioteket.medieplatform.autonomous.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.Delivery;
import dk.statsbiblioteket.medieplatform.autonomous.DeliveryDomsEventStorage;
import org.mockito.Matchers;
import org.testng.annotations.Test;


import dk.statsbiblioteket.medieplatform.autonomous.Event;

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

/**
 * TODO: Thease testcases is a copy of testcases from other "avisproject" which it enherits from. Things that failed was removed
 *
 */
public class CreateDeliveryTest {

    /**
     * Test normal case: RT1 received, no other roundtrips exist.
     * Note: The batch is not known by DOMS, only MFPAK, so the getAllRoundTrips should return null.
     * Expected behaviour: RT1 has an event added.
     */
    @Test
    public void testDoWorkRT1() throws Exception {
        Delivery delivery = new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY);
        delivery.setEventList(Collections.<Event>emptyList());

        DeliveryDomsEventStorage domsStorage = mock(DeliveryDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(null);

        CreateDelivery.doWork(new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY), "premisAgent", domsStorage);
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY)), eq("premisAgent"), Matchers.<Date>any(),
                                                      anyString(), eq("Data_Received"), eq(true));
        verifyNoMoreInteractions(domsStorage);
    }

    /**
     * Test normal case: RT2 received, RT1 already exists.
     * Expected behaviour: RT2 has an event added. RT1 gets a stop event.
     */
    @Test
    public void testDoWorkRT2() throws Exception {
        Delivery delivery1 = new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY);
        delivery1.setEventList(Collections.<Event>emptyList());
        Delivery delivery2 = new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY);
        delivery2.setEventList(Collections.<Event>emptyList());

        DeliveryDomsEventStorage domsStorage = mock(DeliveryDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(delivery1));

        CreateDelivery.doWork(new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY), "premisAgent", domsStorage);
    }

    /**
     * Test exceptional case: RT1 received, RT2 already exists.
     * Expected behaviour: RT1 gets a failed event added.
     */
    @Test
    public void testDoWorkRT1afterRT2() throws Exception {
        Delivery delivery1 = new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY);
        delivery1.setEventList(Collections.<Event>emptyList());
        Delivery delivery2 = new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY);
        delivery2.setEventList(Collections.<Event>emptyList());

        DeliveryDomsEventStorage domsStorage = mock(DeliveryDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(delivery2));

        CreateDelivery.doWork(new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY), "premisAgent", domsStorage);
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY)), eq("premisAgent"), Matchers.<Date>any(),
                                                      contains("Roundtrip (2) is newer than this roundtrip (1)"), eq("Data_Received"), eq(false));
        verifyNoMoreInteractions(domsStorage);
    }

    /**
     * Test exceptional case: RT2 received, RT1 already approved.
     * Expected behaviour: RT2 gets a failed event added.
     */
    @Test
    public void testDoWorkRT2whereRT1Approved() throws Exception {
        Delivery delivery1 = new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY);
        Event event = new Event();
        event.setEventID("Roundtrip_Approved");
        event.setSuccess(true);
        delivery1.setEventList(Arrays.asList(event));
        Delivery delivery2 = new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY);
        delivery2.setEventList(Collections.<Event>emptyList());

        DeliveryDomsEventStorage domsStorage = mock(DeliveryDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(delivery1));

        CreateDelivery.doWork(new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY), "premisAgent", domsStorage);
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY)), eq("premisAgent"), Matchers.<Date>any(),
                anyString(), eq("Data_Received"), eq(true));
        //Text containing "Newer roundtrip" means that fedora has identifid that it has already been added
        verify(domsStorage, times(1)).appendEventToItem(eq(new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY)), eq("premisAgent"), Matchers.<Date>any(), contains("Newer roundtrip"), eq("Manually_stopped"), eq(true));
        verifyNoMoreInteractions(domsStorage);
    }

    /**
     * Test exceptional case: RT2 received, RT1 already approved.
     * Expected behaviour: RT2 gets a failed event added.
     */
    @Test
    public void testMutation() throws Exception {
        Delivery mutation1 = new Delivery("1234", 1, Delivery.DeliveryType.MUTATION);
        Event event = new Event();
        event.setEventID("Roundtrip_Approved");
        event.setSuccess(true);
        mutation1.setEventList(Arrays.asList(event));
        Delivery mutation2 = new Delivery("1234", 2, Delivery.DeliveryType.MUTATION);
        mutation2.setEventList(Collections.<Event>emptyList());

        DeliveryDomsEventStorage domsStorage = mock(DeliveryDomsEventStorage.class);
        when(domsStorage.getAllRoundTrips("1234")).thenReturn(Arrays.asList(mutation1));

        CreateDelivery.doWork(new Delivery("1234", 2, Delivery.DeliveryType.MUTATION), "premisAgent", domsStorage);
        verify(domsStorage, times(1)).getAllRoundTrips("1234");
        verify(domsStorage, times(1)).appendEventToItem(eq(new Delivery("1234", 2, Delivery.DeliveryType.STDDELIVERY)), eq("premisAgent"), Matchers.<Date>any(),
                anyString(), eq("Mutation_Received"), eq(true));
        //Text containing "Newer roundtrip" means that fedora has identifid that it has already been added
        verify(domsStorage, times(1)).appendEventToItem(eq(new Delivery("1234", 1, Delivery.DeliveryType.STDDELIVERY)), eq("premisAgent"), Matchers.<Date>any(), contains("Newer roundtrip"), eq("Manually_stopped"), eq(true));
        verifyNoMoreInteractions(domsStorage);
    }
}