package io.vega.flink.operators;

import io.vega.flink.models.EnrichedSLNewsArticle;
import io.vega.flink.models.RawSLNewsArticle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewsEnricherTest {

    @Test
    void categorizesDisasterNews() {
        RawSLNewsArticle article = sample("Breaking: Flood warnings across Western Province", "Heavy rains continue");
        EnrichedSLNewsArticle enriched = NewsEnricher.enrich(article);

        assertEquals("disaster", enriched.category());
        assertTrue(enriched.isBreaking());
    }

    @Test
    void categorizesPoliticsNews() {
        RawSLNewsArticle article = sample("Parliament session today", "Minister addresses assembly");
        assertEquals("politics", NewsEnricher.enrich(article).category());
    }

    @Test
    void countsWords() {
        RawSLNewsArticle article = sample("One two three", "four five");
        assertEquals(5, NewsEnricher.enrich(article).wordCount());
    }

    @Test
    void preservesLanguage() {
        RawSLNewsArticle article = new RawSLNewsArticle(
                "si-1", "ශීර්ෂය", "විස්තර", "https://example.com", "feed", "Source",
                1_700_000_000_000L, 1_700_000_000_000L, "si");
        assertEquals("si", NewsEnricher.enrich(article).language());
    }

    private static RawSLNewsArticle sample(String title, String description) {
        return new RawSLNewsArticle(
                "id-1", title, description, "https://example.com", "https://feed.test", "Test",
                1_700_000_000_000L, 1_700_000_000_000L, "en");
    }
}
