package io.vega.flink.models;

public record EditAnomaly(
        String title,
        String user,
        String wiki,
        long timestamp,
        AnomalyType anomalyType,
        int editSizeDelta,
        int editCount
) {
    public enum AnomalyType {
        LARGE_EDIT,
        RAPID_EDITS
    }
}
