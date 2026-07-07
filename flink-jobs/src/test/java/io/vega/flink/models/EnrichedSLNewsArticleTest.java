package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrichedSLNewsArticleTest {

    @Test
    void recordHoldsEnrichmentFields() {
        EnrichedSLNewsArticle article = new EnrichedSLNewsArticle(
                "abc-123", "Headline", "Summary text here", "https://news.lk/1",
                "https://feed.lk/rss", "Daily News", 1_700_000_000_000L,
                1_700_000_001_000L, "en", "politics", 4, true);

        assertEquals("politics", article.category());
        assertEquals(4, article.wordCount());
        assertTrue(article.isBreaking());
    }
}
