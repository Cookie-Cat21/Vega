package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventCorrelationTest {

    @Test
    void recordHoldsCorrelationFields() {
        EventCorrelation correlation = new EventCorrelation(
                "EONET-1", "Wildfire", "wildfires", "Bushfire",
                42, 1_700_000_000_000L, 300, 1_700_000_000_000L, 1_700_000_180_000L);

        assertEquals(42, correlation.editCount());
        assertEquals(300, correlation.reactionTimeSeconds());
    }
}
