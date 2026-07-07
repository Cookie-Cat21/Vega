package io.vega.flink.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SLNewsPipelineIT {

    @Test
    @Disabled("requires running Kafka, Schema Registry, and Flink cluster")
    void slNewsPipelineEndToEnd() {
        // Integration test stub: ingest raw-sl-news through SLNewsEnrichmentJob
    }
}
