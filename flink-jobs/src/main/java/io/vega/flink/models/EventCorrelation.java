package io.vega.flink.models;

public record EventCorrelation(
        String naturalEventId,
        String naturalEventTitle,
        String category,
        String wikiArticleTitle,
        long editCount,
        long firstEditTimestamp,
        long reactionTimeSeconds,
        long windowStart,
        long windowEnd
) {}
