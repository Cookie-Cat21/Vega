package io.vega.connector.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DeadLetterPublisherTest {

    @Test
    void escapeHandlesQuotes() {
        assertDoesNotThrow(() -> {
            try (DeadLetterPublisher publisher = new DeadLetterPublisher("localhost:9092", "test-dlq")) {
                publisher.publish("slnews", "{\"bad\":true}", "parse error \"quoted\"");
            }
        });
    }
}
