package io.vega.flink.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NaturalEventMetricsMapperTest {

    @Test
    void metricNameIsNaturalEventsTotal() {
        NaturalEventMetricsMapper mapper = new NaturalEventMetricsMapper();
        assertEquals("natural_events_total", mapper.metricName());
    }
}
