package io.vega.connector.wikimedia;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WikimediaSourceConfigTest {

    @Test
    void defaultsAreApplied() {
        WikimediaSourceConfig config = new WikimediaSourceConfig(Map.of());

        assertEquals("raw-wiki-events", config.topic());
        assertEquals("https://stream.wikimedia.org/v2/stream/recentchange", config.sseUrl());
        assertEquals(100, config.batchSize());
        assertEquals(1000L, config.pollTimeoutMs());
    }

    @Test
    void customValuesAreAccepted() {
        WikimediaSourceConfig config = new WikimediaSourceConfig(Map.of(
                WikimediaSourceConfig.TOPIC_CONFIG, "custom-topic",
                WikimediaSourceConfig.SSE_URL_CONFIG, "http://localhost/events",
                WikimediaSourceConfig.BATCH_SIZE_CONFIG, "50",
                WikimediaSourceConfig.POLL_TIMEOUT_MS_CONFIG, "500"
        ));

        assertEquals("custom-topic", config.topic());
        assertEquals("http://localhost/events", config.sseUrl());
        assertEquals(50, config.batchSize());
        assertEquals(500L, config.pollTimeoutMs());
    }

    @Test
    void rejectsInvalidBatchSize() {
        assertThrows(Exception.class, () -> new WikimediaSourceConfig(Map.of(
                WikimediaSourceConfig.BATCH_SIZE_CONFIG, "0"
        )));
    }
}
