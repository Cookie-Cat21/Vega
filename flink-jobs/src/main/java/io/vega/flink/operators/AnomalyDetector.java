package io.vega.flink.operators;

import io.vega.flink.models.EditAnomaly;
import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.RawWikiEvent;

import java.util.ArrayList;
import java.util.List;

public final class AnomalyDetector {

    public static final int LARGE_EDIT_THRESHOLD = 10_000;
    public static final int RAPID_EDIT_THRESHOLD = 5;
    public static final long RAPID_EDIT_WINDOW_MS = 60_000L;

    private AnomalyDetector() {}

    public static List<EditAnomaly> detectLargeEdit(RawWikiEvent event) {
        EnrichedWikiEvent enriched = EditEnricher.enrich(event);
        if (enriched.editSizeDelta() > LARGE_EDIT_THRESHOLD) {
            return List.of(new EditAnomaly(
                    event.title(),
                    event.user(),
                    event.wiki(),
                    event.timestamp(),
                    EditAnomaly.AnomalyType.LARGE_EDIT,
                    enriched.editSizeDelta(),
                    1
            ));
        }
        return List.of();
    }

    public static List<EditAnomaly> detectRapidEdits(String user, List<RawWikiEvent> recentEdits, long nowMs) {
        List<RawWikiEvent> inWindow = recentEdits.stream()
                .filter(e -> e.timestamp() >= nowMs - RAPID_EDIT_WINDOW_MS)
                .toList();

        if (inWindow.size() > RAPID_EDIT_THRESHOLD) {
            RawWikiEvent latest = inWindow.get(inWindow.size() - 1);
            return List.of(new EditAnomaly(
                    latest.title(),
                    user,
                    latest.wiki(),
                    latest.timestamp(),
                    EditAnomaly.AnomalyType.RAPID_EDITS,
                    EditEnricher.enrich(latest).editSizeDelta(),
                    inWindow.size()
            ));
        }
        return List.of();
    }

    public static List<EditAnomaly> detectAll(RawWikiEvent event, List<RawWikiEvent> userHistory) {
        List<EditAnomaly> anomalies = new ArrayList<>(detectLargeEdit(event));
        anomalies.addAll(detectRapidEdits(event.user(), userHistory, event.timestamp()));
        return anomalies;
    }
}
