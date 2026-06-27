package io.vega.flink.models;

public record NaturalEvent(
        String eventId,
        String title,
        String description,
        String category,
        String sourceUrl,
        double latitude,
        double longitude,
        long eventDate,
        Double magnitudeValue,
        String magnitudeUnit,
        boolean isClosed,
        long ingestedAt
) {}
