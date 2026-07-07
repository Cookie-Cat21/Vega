package io.vega.connector.wikimedia;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikimediaSSEClientParseTest {

    @Test
    void parseEventRejectsInvalidJson() {
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", new LinkedBlockingQueue<>());

        assertThrows(IOException.class, () -> client.parseEvent("{not valid json"));
    }

    @Test
    void parseEventRejectsTruncatedJson() {
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", new LinkedBlockingQueue<>());

        assertThrows(IOException.class, () -> client.parseEvent("{\"type\":\"edit\","));
    }

    @Test
    void processEventHandlesInvalidJsonGracefully() {
        LinkedBlockingQueue<WikiEvent> queue = new LinkedBlockingQueue<>();
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", queue);

        assertDoesNotThrow(() -> client.processEvent("message", "{broken"));
        assertTrue(queue.isEmpty());
    }
}
