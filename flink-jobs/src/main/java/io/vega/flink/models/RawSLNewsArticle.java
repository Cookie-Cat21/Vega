package io.vega.flink.models;

public record RawSLNewsArticle(
        String articleId,
        String title,
        String description,
        String link,
        String sourceFeed,
        String sourceName,
        long publishedAt,
        long ingestedAt,
        String language
) {}
