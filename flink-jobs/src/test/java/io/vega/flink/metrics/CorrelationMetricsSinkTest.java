package io.vega.flink.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorrelationMetricsSinkTest {

    @Test
    void sinkClassIsInstantiable() {
        assertNotNull(new CorrelationMetricsSink());
    }
}
