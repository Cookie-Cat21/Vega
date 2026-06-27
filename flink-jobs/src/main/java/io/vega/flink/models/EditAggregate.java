package io.vega.flink.models;

public record EditAggregate(
        String wiki,
        long windowStart,
        long windowEnd,
        long totalEdits,
        long botEdits,
        long humanEdits,
        double avgEditSize
) {}
