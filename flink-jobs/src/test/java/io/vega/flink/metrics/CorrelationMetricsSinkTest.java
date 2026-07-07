package io.vega.flink.metrics;

import io.vega.flink.models.EventCorrelation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorrelationMetricsSinkTest {

    @Test
    void sinkClassIsInstantiable() {
        assertNotNull(new CorrelationMetricsSink());
    }

    @Test
    void invokeAcceptsCorrelation() {
        CorrelationMetricsSink sink = new CorrelationMetricsSink();
        EventCorrelation correlation = new EventCorrelation(
                "E1", "Fire", "wildfires", "Bushfire", 5,
                1_700_000_000_000L, 120, 1_700_000_000_000L, 1_700_000_180_000L);
        sink.invoke(correlation, null);
    }
}
