package io.vega.connector.eonet;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EONETSourceTaskTest {

    private EONETSourceTask task;
    private EONETRestClient restClient;

    @BeforeEach
    void setUp() {
        task = new EONETSourceTask();
        task.setConfig(new EONETSourceConfig(Map.of(
                EONETSourceConfig.TOPIC_CONFIG, "raw-natural-events",
                EONETSourceConfig.POLL_INTERVAL_MS_CONFIG, "1000"
        )));
        restClient = mock(EONETRestClient.class);
        task.setRestClient(restClient);
        task.setLastPollMs(0L);
    }

    @Test
    void pollProducesOnlyNewEvents() throws Exception {
        NaturalEvent event1 = sampleEvent("E1", 10.0, 20.0, 1000L);
        NaturalEvent event2 = sampleEvent("E2", 11.0, 21.0, 2000L);

        when(restClient.fetchEvents())
                .thenReturn(List.of(event1, event2))
                .thenReturn(List.of(event1, event2));

        List<SourceRecord> firstPoll = task.poll();
        assertEquals(2, firstPoll.size());

        task.setLastPollMs(0L);
        List<SourceRecord> secondPoll = task.poll();
        assertTrue(secondPoll.isEmpty());
        verify(restClient, times(2)).fetchEvents();
    }

    @Test
    void pollDetectsNewGeometryAsNewRecord() throws Exception {
        NaturalEvent initial = sampleEvent("E1", 10.0, 20.0, 1000L);
        NaturalEvent updated = sampleEvent("E1", 12.0, 22.0, 2000L);

        when(restClient.fetchEvents())
                .thenReturn(List.of(initial))
                .thenReturn(List.of(initial, updated));

        assertEquals(1, task.poll().size());
        task.setLastPollMs(0L);
        assertEquals(1, task.poll().size());
    }

    @Test
    void eventKeyIsStable() {
        NaturalEvent event = sampleEvent("E1", 10.0, 20.0, 1000L);
        assertEquals("E1:10.0:20.0:1000", EONETSourceTask.eventKey(event));
    }

    @Test
    void deduplicationWithPreloadedOffsets() throws Exception {
        NaturalEvent event = sampleEvent("E1", 10.0, 20.0, 1000L);
        task.setSeenEventKeys(Set.of(EONETSourceTask.eventKey(event)));
        when(restClient.fetchEvents()).thenReturn(List.of(event));

        assertTrue(task.poll().isEmpty());
    }

    private static NaturalEvent sampleEvent(String id, double lat, double lon, long eventDate) {
        NaturalEvent event = new NaturalEvent();
        event.setEventId(id);
        event.setTitle("Event " + id);
        event.setCategory("Wildfires");
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setEventDate(eventDate);
        event.setIsClosed(false);
        event.setIngestedAt(System.currentTimeMillis());
        return event;
    }
}
