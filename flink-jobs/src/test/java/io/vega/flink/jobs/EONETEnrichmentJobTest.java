package io.vega.flink.jobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EONETEnrichmentJobTest {

    @Test
    void mainMethodExists() throws Exception {
        assertNotNull(EONETEnrichmentJob.class.getMethod("main", String[].class));
    }
}
