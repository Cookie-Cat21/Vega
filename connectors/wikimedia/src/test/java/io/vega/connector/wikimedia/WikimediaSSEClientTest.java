package io.vega.connector.wikimedia;

import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikimediaSSEClientTest {

    @Test
    void parseEventExtractsEditFields() throws Exception {
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", new LinkedBlockingQueue<>());
        String json = """
                {
                  "id": 12345,
                  "type": "edit",
                  "namespace": 0,
                  "title": "Earth",
                  "comment": "fixed typo",
                  "timestamp": 1700000000,
                  "user": "alice",
                  "bot": false,
                  "server_url": "https://en.wikipedia.org",
                  "wiki": "enwiki",
                  "length": {"old": 1000, "new": 1050},
                  "revision": {"old": 100, "new": 101}
                }
                """;

        WikiEvent event = client.parseEvent(json);

        assertEquals(12345L, event.getId());
        assertEquals("edit", event.getType());
        assertEquals("Earth", event.getTitle());
        assertEquals("alice", event.getUser());
        assertEquals(false, event.getBot());
        assertEquals("enwiki", event.getWiki());
        assertEquals(1_700_000_000_000L, event.getTimestamp());
        assertEquals(1000, event.getLengthOld());
        assertEquals(1050, event.getLengthNew());
        assertEquals(100L, event.getRevisionOld());
        assertEquals(101L, event.getRevisionNew());
    }

    @Test
    void parseEventIgnoresNonEditTypes() throws Exception {
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", new LinkedBlockingQueue<>());
        String json = """
                {"type": "log", "title": "User:alice", "timestamp": 1700000000}
                """;

        assertNull(client.parseEvent(json));
    }

    @Test
    void processEventEnqueuesEditMessages() {
        LinkedBlockingQueue<WikiEvent> queue = new LinkedBlockingQueue<>();
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", queue);
        String data = """
                {"type":"edit","title":"Moon","user":"bob","bot":false,"wiki":"enwiki",
                 "server_url":"https://en.wikipedia.org","timestamp":1700000000,"namespace":0}
                """;

        client.processEvent("message", data);

        assertEquals(1, queue.size());
        assertEquals("Moon", queue.peek().getTitle());
    }

    @Test
    void processEventSkipsNonMessageEventTypes() {
        LinkedBlockingQueue<WikiEvent> queue = new LinkedBlockingQueue<>();
        WikimediaSSEClient client = new WikimediaSSEClient("http://localhost", queue);
        String data = """
                {"type":"edit","title":"Moon","user":"bob","bot":false,"wiki":"enwiki",
                 "server_url":"https://en.wikipedia.org","timestamp":1700000000,"namespace":0}
                """;

        client.processEvent("heartbeat", data);

        assertTrue(queue.isEmpty());
    }
}
