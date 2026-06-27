package io.vega.flink.operators;

import io.vega.flink.models.EditAnomaly;
import io.vega.flink.models.RawWikiEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyDetectorTest {

    @Test
    void detectsLargeEdit() {
        RawWikiEvent event = new RawWikiEvent(
                1L, "Big", "user", false, "enwiki", "url",
                1_700_000_000_000L, "edit", 0, null, 0, 15_000, null, null);

        List<EditAnomaly> anomalies = AnomalyDetector.detectLargeEdit(event);

        assertEquals(1, anomalies.size());
        assertEquals(EditAnomaly.AnomalyType.LARGE_EDIT, anomalies.getFirst().anomalyType());
    }

    @Test
    void detectsRapidEdits() {
        long base = 1_700_000_000_000L;
        List<RawWikiEvent> history = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            history.add(new RawWikiEvent(
                    (long) i, "Page" + i, "rapid-user", false, "enwiki", "url",
                    base + i * 1000, "edit", 0, null, 100, 110, null, null));
        }

        List<EditAnomaly> anomalies = AnomalyDetector.detectRapidEdits("rapid-user", history, base + 5000);

        assertEquals(1, anomalies.size());
        assertEquals(EditAnomaly.AnomalyType.RAPID_EDITS, anomalies.getFirst().anomalyType());
        assertTrue(anomalies.getFirst().editCount() > 5);
    }

    @Test
    void ignoresSmallEdits() {
        RawWikiEvent event = new RawWikiEvent(
                1L, "Small", "user", false, "enwiki", "url",
                1_700_000_000_000L, "edit", 0, null, 100, 150, null, null);

        assertTrue(AnomalyDetector.detectLargeEdit(event).isEmpty());
    }
}
