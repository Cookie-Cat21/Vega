package io.vega.flink.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class EONETPipelineIT {

    @Test
    @Disabled("requires running Kafka, Schema Registry, and Flink cluster")
    void eonetPipelineEndToEnd() {
        // Integration test stub: ingest raw-natural-events through EONETEnrichmentJob
    }
}
