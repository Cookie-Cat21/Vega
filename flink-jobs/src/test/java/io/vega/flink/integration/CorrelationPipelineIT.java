package io.vega.flink.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CorrelationPipelineIT {

    @Test
    @Disabled("requires running Kafka, Schema Registry, and Flink cluster")
    void correlationPipelineEndToEnd() {
        // Integration test stub: dual-stream correlation across natural events and wiki edits
    }
}
