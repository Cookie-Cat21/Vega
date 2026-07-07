package io.vega.flink.jobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SLNewsEnrichmentJobTest {

    @Test
    void mainMethodExists() throws Exception {
        assertNotNull(SLNewsEnrichmentJob.class.getMethod("main", String[].class));
    }
}
