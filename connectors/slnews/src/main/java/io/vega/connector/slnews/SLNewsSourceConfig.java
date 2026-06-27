package io.vega.connector.slnews;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SLNewsSourceConfig extends AbstractConfig {

    public static final String TOPIC_CONFIG = "topic";
    public static final String FEED_URLS_CONFIG = "feed.urls";
    public static final String POLL_INTERVAL_MS_CONFIG = "poll.interval.ms";

    private static final String DEFAULT_TOPIC = "raw-sl-news";
    private static final String DEFAULT_FEEDS = String.join(",",
            "https://www.adaderana.lk/rss.php",
            "https://www.dailymirror.lk/RSS/rss.xml",
            "https://www.newsfirst.lk/feed/");

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(TOPIC_CONFIG, ConfigDef.Type.STRING, DEFAULT_TOPIC, ConfigDef.Importance.HIGH,
                    "Kafka topic to publish Sri Lanka news articles to")
            .define(FEED_URLS_CONFIG, ConfigDef.Type.STRING, DEFAULT_FEEDS, ConfigDef.Importance.MEDIUM,
                    "Comma-separated list of RSS feed URLs")
            .define(POLL_INTERVAL_MS_CONFIG, ConfigDef.Type.LONG, 300_000L, ConfigDef.Range.atLeast(60_000),
                    ConfigDef.Importance.MEDIUM, "Polling interval in milliseconds (default 5 minutes)");

    public SLNewsSourceConfig(Map<String, ?> props) {
        super(CONFIG_DEF, props);
    }

    public String topic() {
        return getString(TOPIC_CONFIG);
    }

    public List<String> feedUrls() {
        return Arrays.stream(getString(FEED_URLS_CONFIG).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public long pollIntervalMs() {
        return getLong(POLL_INTERVAL_MS_CONFIG);
    }
}
