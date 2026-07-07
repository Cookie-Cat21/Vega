package io.vega.flink.jobs;

import io.vega.flink.models.EditAnomaly;
import io.vega.flink.models.RawWikiEvent;
import org.apache.flink.util.Collector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnomalyDetectionJobLargeEditTest {

    @Test
    void largeEditFlatMapEmitsAnomalyForLargeEdit() throws Exception {
        RawWikiEvent event = new RawWikiEvent(
                1L, "Big", "user", false, "enwiki", "url",
                1_700_000_000_000L, "edit", 0, null, 0, 15_000, null, null);

        List<EditAnomaly> collected = new ArrayList<>();
        Collector<EditAnomaly> collector = new Collector<>() {
            @Override
            public void collect(EditAnomaly record) {
                collected.add(record);
            }

            @Override
            public void close() {
            }
        };

        new AnomalyDetectionJob.LargeEditFlatMap().flatMap(event, collector);

        assertEquals(1, collected.size());
        assertEquals(EditAnomaly.AnomalyType.LARGE_EDIT, collected.getFirst().anomalyType());
    }
}
