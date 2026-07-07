package io.vega.flink.jobs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnomalyDetectionJobTest {

    @Test
    void mainMethodExists() throws Exception {
        assertNotNull(AnomalyDetectionJob.class.getMethod("main", String[].class));
    }
}
