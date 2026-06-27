package io.vega.connector.eonet;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EONETSourceTask extends SourceTask {

    private static final Logger LOG = LoggerFactory.getLogger(EONETSourceTask.class);
    private static final Map<String, String> SOURCE_PARTITION = Collections.singletonMap("source", "eonet");

    private EONETSourceConfig config;
    private EONETRestClient restClient;
    private long lastPollMs;
    private Set<String> seenEventKeys = new HashSet<>();

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new EONETSourceConfig(props);
        restClient = new EONETRestClient(config.apiUrl(), config.daysLookback(), config.status());
        lastPollMs = 0L;

        Map<String, Object> offset = context.offsetStorageReader().offset(SOURCE_PARTITION);
        if (offset != null && offset.get("last_event_ids") != null) {
            String ids = offset.get("last_event_ids").toString();
            if (!ids.isBlank()) {
                seenEventKeys = new LinkedHashSet<>(List.of(ids.split(",")));
            }
        }

        LOG.info("EONET source task started, topic={}", config.topic());
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        long now = System.currentTimeMillis();
        if (now - lastPollMs < config.pollIntervalMs()) {
            Thread.sleep(Math.min(1_000L, config.pollIntervalMs() - (now - lastPollMs)));
            return Collections.emptyList();
        }

        lastPollMs = now;
        List<SourceRecord> records = new ArrayList<>();

        try {
            List<NaturalEvent> events = restClient.fetchEvents();
            Set<String> currentKeys = new LinkedHashSet<>();

            for (NaturalEvent event : events) {
                String key = eventKey(event);
                currentKeys.add(key);

                if (!seenEventKeys.contains(key)) {
                    records.add(toSourceRecord(event, currentKeys));
                }
            }

            seenEventKeys = currentKeys;
            if (!records.isEmpty()) {
                LOG.info("Produced {} new EONET records", records.size());
            }
        } catch (IOException e) {
            LOG.error("Failed to poll EONET API: {}", e.getMessage());
        }

        return records;
    }

    private SourceRecord toSourceRecord(NaturalEvent event, Set<String> currentKeys) {
        String offsetValue = currentKeys.stream().collect(Collectors.joining(","));
        Map<String, String> sourceOffset = Collections.singletonMap("last_event_ids", offsetValue);
        SchemaAndValue value = AvroConnect.toConnect(event);

        return new SourceRecord(
                SOURCE_PARTITION,
                sourceOffset,
                config.topic(),
                Schema.STRING_SCHEMA,
                event.getEventId(),
                value.schema(),
                value.value()
        );
    }

    static String eventKey(NaturalEvent event) {
        return event.getEventId() + ":" + event.getLatitude() + ":" + event.getLongitude()
                + ":" + event.getEventDate();
    }

    @Override
    public void stop() {
        LOG.info("EONET source task stopped");
    }

    void setRestClient(EONETRestClient client) {
        this.restClient = client;
    }

    void setConfig(EONETSourceConfig config) {
        this.config = config;
    }

    void setSeenEventKeys(Set<String> keys) {
        this.seenEventKeys = new HashSet<>(keys);
    }

    void setLastPollMs(long lastPollMs) {
        this.lastPollMs = lastPollMs;
    }
}
