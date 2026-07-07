package io.vega.flink.jobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WikiEnrichmentJobTest {

    @Test
    void mainMethodExists() throws Exception {
        assertNotNull(WikiEnrichmentJob.class.getMethod("main", String[].class));
    }
}
