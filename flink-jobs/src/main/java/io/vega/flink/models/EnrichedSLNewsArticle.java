package io.vega.flink.models;

public record EnrichedSLNewsArticle(
        String articleId,
        String title,
        String description,
        String link,
        String sourceFeed,
        String sourceName,
        long publishedAt,
        long ingestedAt,
        String language,
        String category,
        int wordCount,
        boolean isBreaking
) {}
