package io.vega.connector.eonet;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class EONETSourceConfig extends AbstractConfig {

    public static final String TOPIC_CONFIG = "topic";
    public static final String API_URL_CONFIG = "api.url";
    public static final String POLL_INTERVAL_MS_CONFIG = "poll.interval.ms";
    public static final String DAYS_LOOKBACK_CONFIG = "days.lookback";
    public static final String STATUS_CONFIG = "status";

    private static final String DEFAULT_API_URL = "https://eonet.gsfc.nasa.gov/api/v3/events";
    private static final String DEFAULT_TOPIC = "raw-natural-events";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(TOPIC_CONFIG, ConfigDef.Type.STRING, DEFAULT_TOPIC, ConfigDef.Importance.HIGH,
                    "Kafka topic to publish natural events to")
            .define(API_URL_CONFIG, ConfigDef.Type.STRING, DEFAULT_API_URL, ConfigDef.Importance.MEDIUM,
                    "NASA EONET API endpoint URL")
            .define(POLL_INTERVAL_MS_CONFIG, ConfigDef.Type.LONG, 60_000L, ConfigDef.Range.atLeast(1000),
                    ConfigDef.Importance.MEDIUM, "Polling interval in milliseconds")
            .define(DAYS_LOOKBACK_CONFIG, ConfigDef.Type.INT, 30, ConfigDef.Range.atLeast(1),
                    ConfigDef.Importance.MEDIUM, "Number of days to look back for events")
            .define(STATUS_CONFIG, ConfigDef.Type.STRING, "open", ConfigDef.ValidString.in("open", "closed", "all"),
                    ConfigDef.Importance.MEDIUM, "Event status filter: open, closed, or all");

    public EONETSourceConfig(Map<String, ?> props) {
        super(CONFIG_DEF, props);
    }

    public String topic() {
        return getString(TOPIC_CONFIG);
    }

    public String apiUrl() {
        return getString(API_URL_CONFIG);
    }

    public long pollIntervalMs() {
        return getLong(POLL_INTERVAL_MS_CONFIG);
    }

    public int daysLookback() {
        return getInt(DAYS_LOOKBACK_CONFIG);
    }

    public String status() {
        return getString(STATUS_CONFIG);
    }
}
