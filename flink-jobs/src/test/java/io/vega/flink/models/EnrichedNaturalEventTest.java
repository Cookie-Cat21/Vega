package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnrichedNaturalEventTest {

    @Test
    void recordHoldsEnrichmentFields() {
        EnrichedNaturalEvent event = new EnrichedNaturalEvent(
                "EONET-1", "Flood", "River flood", "floods",
                "https://eonet.gsfc.nasa.gov", 6.9, 79.8,
                1_700_000_000_000L, 5.2, "m", false, 1_700_000_001_000L,
                "South Asia", "moderate");

        assertEquals("South Asia", event.regionName());
        assertEquals("moderate", event.severityLabel());
    }
}
