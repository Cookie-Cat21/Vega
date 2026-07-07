package io.vega.flink.jobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorrelationJobTest {

    @Test
    void mainMethodExists() throws Exception {
        assertNotNull(CorrelationJob.class.getMethod("main", String[].class));
    }
}
