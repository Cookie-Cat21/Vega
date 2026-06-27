package io.vega.connector.wikimedia;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class WikimediaSourceConfig extends AbstractConfig {

    public static final String TOPIC_CONFIG = "topic";
    public static final String SSE_URL_CONFIG = "sse.url";
    public static final String BATCH_SIZE_CONFIG = "batch.size";
    public static final String POLL_TIMEOUT_MS_CONFIG = "poll.timeout.ms";

    private static final String DEFAULT_SSE_URL = "https://stream.wikimedia.org/v2/stream/recentchange";
    private static final String DEFAULT_TOPIC = "raw-wiki-events";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(TOPIC_CONFIG, ConfigDef.Type.STRING, DEFAULT_TOPIC, ConfigDef.Importance.HIGH,
                    "Kafka topic to publish wiki events to")
            .define(SSE_URL_CONFIG, ConfigDef.Type.STRING, DEFAULT_SSE_URL, ConfigDef.Importance.MEDIUM,
                    "Wikimedia EventStreams SSE endpoint URL")
            .define(BATCH_SIZE_CONFIG, ConfigDef.Type.INT, 100, ConfigDef.Range.atLeast(1), ConfigDef.Importance.MEDIUM,
                    "Maximum number of events to return per poll() call")
            .define(POLL_TIMEOUT_MS_CONFIG, ConfigDef.Type.LONG, 1000L, ConfigDef.Range.atLeast(0),
                    ConfigDef.Importance.LOW, "Maximum time in ms to wait for events during poll()");

    public WikimediaSourceConfig(Map<String, ?> props) {
        super(CONFIG_DEF, props);
    }

    public String topic() {
        return getString(TOPIC_CONFIG);
    }

    public String sseUrl() {
        return getString(SSE_URL_CONFIG);
    }

    public int batchSize() {
        return getInt(BATCH_SIZE_CONFIG);
    }

    public long pollTimeoutMs() {
        return getLong(POLL_TIMEOUT_MS_CONFIG);
    }
}
