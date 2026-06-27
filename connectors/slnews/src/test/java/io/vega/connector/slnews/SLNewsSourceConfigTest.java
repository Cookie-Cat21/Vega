package io.vega.connector.slnews;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SLNewsSourceConfigTest {

    @Test
    void defaultsAreApplied() {
        SLNewsSourceConfig config = new SLNewsSourceConfig(Map.of());

        assertEquals("raw-sl-news", config.topic());
        assertEquals(300_000L, config.pollIntervalMs());
        assertEquals(3, config.feedUrls().size());
        assertTrue(config.feedUrls().getFirst().contains("adaderana"));
    }

    @Test
    void customFeedUrlsAreParsed() {
        SLNewsSourceConfig config = new SLNewsSourceConfig(Map.of(
                SLNewsSourceConfig.FEED_URLS_CONFIG, "https://example.com/feed, https://other.com/rss"
        ));

        assertEquals(List.of("https://example.com/feed", "https://other.com/rss"), config.feedUrls());
    }

    @Test
    void rejectsPollIntervalBelowMinimum() {
        assertThrows(Exception.class, () -> new SLNewsSourceConfig(Map.of(
                SLNewsSourceConfig.POLL_INTERVAL_MS_CONFIG, "1000"
        )));
    }
}
