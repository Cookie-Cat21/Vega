package io.vega.connector.slnews;

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

public class SLNewsSourceTask extends SourceTask {

    private static final Logger LOG = LoggerFactory.getLogger(SLNewsSourceTask.class);
    private static final Map<String, String> SOURCE_PARTITION = Collections.singletonMap("source", "slnews");

    private SLNewsSourceConfig config;
    private SLNewsRssClient rssClient;
    private long lastPollMs;
    private Set<String> seenArticleIds = new HashSet<>();

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        config = new SLNewsSourceConfig(props);
        rssClient = new SLNewsRssClient(config.feedUrls());
        lastPollMs = 0L;

        Map<String, Object> offset = context.offsetStorageReader().offset(SOURCE_PARTITION);
        if (offset != null && offset.get("last_article_ids") != null) {
            String ids = offset.get("last_article_ids").toString();
            if (!ids.isBlank()) {
                seenArticleIds = new LinkedHashSet<>(List.of(ids.split(",")));
            }
        }

        LOG.info("SL News source task started, topic={}, feeds={}", config.topic(), config.feedUrls().size());
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        long now = System.currentTimeMillis();
        if (now - lastPollMs < config.pollIntervalMs()) {
            Thread.sleep(Math.min(5_000L, config.pollIntervalMs() - (now - lastPollMs)));
            return Collections.emptyList();
        }

        lastPollMs = now;
        List<SourceRecord> records = new ArrayList<>();

        try {
            List<SLNewsArticle> articles = rssClient.fetchArticles();
            Set<String> currentIds = new LinkedHashSet<>();

            for (SLNewsArticle article : articles) {
                String id = article.getArticleId().toString();
                currentIds.add(id);

                if (!seenArticleIds.contains(id)) {
                    records.add(toSourceRecord(article, currentIds));
                }
            }

            seenArticleIds = currentIds;
            if (!records.isEmpty()) {
                LOG.info("Produced {} new SL news articles", records.size());
            }
        } catch (IOException e) {
            LOG.error("Failed to poll RSS feeds: {}", e.getMessage());
        }

        return records;
    }

    private SourceRecord toSourceRecord(SLNewsArticle article, Set<String> currentIds) {
        String offsetValue = currentIds.stream().collect(Collectors.joining(","));
        Map<String, String> sourceOffset = Collections.singletonMap("last_article_ids", offsetValue);
        SchemaAndValue value = AvroConnect.toConnect(article);

        return new SourceRecord(
                SOURCE_PARTITION,
                sourceOffset,
                config.topic(),
                Schema.STRING_SCHEMA,
                article.getArticleId(),
                value.schema(),
                value.value()
        );
    }

    @Override
    public void stop() {
        LOG.info("SL News source task stopped");
    }

    void setRssClient(SLNewsRssClient client) {
        this.rssClient = client;
    }

    void setConfig(SLNewsSourceConfig config) {
        this.config = config;
    }

    void setSeenArticleIds(Set<String> ids) {
        this.seenArticleIds = new HashSet<>(ids);
    }

    void setLastPollMs(long lastPollMs) {
        this.lastPollMs = lastPollMs;
    }
}
