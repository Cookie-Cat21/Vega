package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RawSLNewsArticleTest {

    @Test
    void recordHoldsArticleFields() {
        RawSLNewsArticle article = new RawSLNewsArticle(
                "abc-123", "Headline", "Summary", "https://news.lk/1",
                "https://feed.lk/rss", "Daily News", 1_700_000_000_000L,
                1_700_000_001_000L, "en");

        assertEquals("abc-123", article.articleId());
        assertEquals("en", article.language());
    }
}
