package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NaturalEventTest {

    @Test
    void recordHoldsGeoFields() {
        NaturalEvent event = new NaturalEvent(
                "EONET-123", "Wildfire", "Bush fire", "wildfires",
                "https://eonet.gsfc.nasa.gov", -33.8, 151.2,
                1_700_000_000_000L, null, null, false, 1_700_000_001_000L);

        assertEquals("EONET-123", event.eventId());
        assertEquals(-33.8, event.latitude());
        assertFalse(event.isClosed());
    }
}
