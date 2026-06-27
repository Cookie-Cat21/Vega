package io.vega.flink.operators;

import io.vega.flink.models.EnrichedSLNewsArticle;
import io.vega.flink.models.RawSLNewsArticle;

import java.util.Locale;

public final class NewsEnricher {

    private NewsEnricher() {}

    public static EnrichedSLNewsArticle enrich(RawSLNewsArticle article) {
        String text = (article.title() != null ? article.title() : "")
                + " " + (article.description() != null ? article.description() : "");

        return new EnrichedSLNewsArticle(
                article.articleId(),
                article.title(),
                article.description(),
                article.link(),
                article.sourceFeed(),
                article.sourceName(),
                article.publishedAt(),
                article.ingestedAt(),
                article.language(),
                categorize(text),
                wordCount(text),
                isBreaking(article.title())
        );
    }

    static String categorize(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "flood", "cyclone", "earthquake", "landslide", "disaster")) {
            return "disaster";
        }
        if (containsAny(lower, "parliament", "election", "minister", "government", "president")) {
            return "politics";
        }
        if (containsAny(lower, "cricket", "rugby", "football", "sport", "match")) {
            return "sports";
        }
        if (containsAny(lower, "rupee", "stock", "economy", "business", "trade")) {
            return "business";
        }
        return "general";
    }

    static int wordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    static boolean isBreaking(String title) {
        if (title == null) {
            return false;
        }
        String lower = title.toLowerCase(Locale.ROOT);
        return lower.contains("breaking") || lower.contains("urgent") || lower.contains("live");
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
