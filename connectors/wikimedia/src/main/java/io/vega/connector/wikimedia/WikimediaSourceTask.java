package io.vega.connector.wikimedia;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WikimediaSourceTask extends SourceTask {

    private static final Logger LOG = LoggerFactory.getLogger(WikimediaSourceTask.class);
    private static final Map<String, String> SOURCE_PARTITION = Collections.singletonMap("stream", "wikimedia");

    private WikimediaSourceConfig config;
    private WikimediaSSEClient sseClient;
    private BlockingQueue<WikiEvent> eventQueue;
    private long lastTimestamp;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new WikimediaSourceConfig(props);
        sseClient = new WikimediaSSEClient(config.sseUrl());
        eventQueue = sseClient.getEventQueue();
        sseClient.start();
        LOG.info("Wikimedia source task started, topic={}", config.topic());
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        List<SourceRecord> records = new ArrayList<>(config.batchSize());
        WikiEvent event = eventQueue.poll(config.pollTimeoutMs(), TimeUnit.MILLISECONDS);

        if (event != null) {
            records.add(toSourceRecord(event));
            while (records.size() < config.batchSize()) {
                WikiEvent next = eventQueue.poll();
                if (next == null) {
                    break;
                }
                records.add(toSourceRecord(next));
            }
        }

        return records;
    }

    private SourceRecord toSourceRecord(WikiEvent event) {
        lastTimestamp = event.getTimestamp();
        Map<String, Long> sourceOffset = Collections.singletonMap("position", lastTimestamp);

        return new SourceRecord(
                SOURCE_PARTITION,
                sourceOffset,
                config.topic(),
                Schema.STRING_SCHEMA,
                event.getTitle(),
                null,
                event
        );
    }

    @Override
    public void stop() {
        if (sseClient != null) {
            sseClient.stop();
        }
        LOG.info("Wikimedia source task stopped");
    }

    void setSseClient(WikimediaSSEClient client) {
        this.sseClient = client;
        this.eventQueue = client.getEventQueue();
    }

    void setConfig(WikimediaSourceConfig config) {
        this.config = config;
    }
}
