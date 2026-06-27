package io.vega.connector.wikimedia;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikimediaSourceTaskTest {

    private WikimediaSourceTask task;
    private LinkedBlockingQueue<WikiEvent> queue;
    private WikimediaSSEClient client;

    @BeforeEach
    void setUp() {
        queue = new LinkedBlockingQueue<>();
        client = new WikimediaSSEClient("http://localhost", queue);
        task = new WikimediaSourceTask();
        task.setConfig(new WikimediaSourceConfig(Map.of(
                WikimediaSourceConfig.TOPIC_CONFIG, "raw-wiki-events",
                WikimediaSourceConfig.BATCH_SIZE_CONFIG, "10",
                WikimediaSourceConfig.POLL_TIMEOUT_MS_CONFIG, "100"
        )));
        task.setSseClient(client);
    }

    @Test
    void pollReturnsEmptyListWhenQueueIsEmpty() throws InterruptedException {
        List<SourceRecord> records = task.poll();
        assertTrue(records.isEmpty());
    }

    @Test
    void pollReturnsRecordsWhenQueueHasEvents() throws InterruptedException {
        WikiEvent event = sampleEvent("Article", 1_700_000_000_000L);
        queue.offer(event);

        List<SourceRecord> records = task.poll();

        assertEquals(1, records.size());
        SourceRecord record = records.getFirst();
        assertEquals("raw-wiki-events", record.topic());
        assertInstanceOf(WikiEvent.class, record.value());
        WikiEvent value = (WikiEvent) record.value();
        assertEquals("Article", value.getTitle());
        assertEquals("enwiki", value.getWiki());
    }

    @Test
    void pollRespectsBatchSize() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            queue.offer(sampleEvent("Article-" + i, 1_700_000_000_000L + i));
        }

        List<SourceRecord> records = task.poll();

        assertEquals(5, records.size());
    }

    @Test
    void recordOffsetTracksLastTimestamp() throws InterruptedException {
        long timestamp = 1_700_000_123_000L;
        queue.offer(sampleEvent("OffsetTest", timestamp));

        SourceRecord record = task.poll().getFirst();

        @SuppressWarnings("unchecked")
        Map<String, Long> offset = (Map<String, Long>) record.sourceOffset();
        assertEquals(timestamp, offset.get("position"));
    }

    @Test
    void recordValueMatchesAvroSchema() throws InterruptedException {
        queue.offer(sampleEvent("SchemaTest", 1_700_000_000_000L));

        WikiEvent value = (WikiEvent) task.poll().getFirst().value();

        assertEquals(WikiEvent.getClassSchema(), value.getSchema());
        assertEquals("SchemaTest", value.getTitle());
        assertEquals("editor", value.getUser());
        assertEquals(false, value.getBot());
    }

    private static WikiEvent sampleEvent(String title, long timestamp) {
        WikiEvent event = new WikiEvent();
        event.setTitle(title);
        event.setUser("editor");
        event.setBot(false);
        event.setWiki("enwiki");
        event.setServerUrl("https://en.wikipedia.org");
        event.setTimestamp(timestamp);
        event.setType("edit");
        event.setNamespace(0);
        event.setComment("test edit");
        event.setLengthOld(100);
        event.setLengthNew(150);
        event.setRevisionOld(1L);
        event.setRevisionNew(2L);
        return event;
    }
}
