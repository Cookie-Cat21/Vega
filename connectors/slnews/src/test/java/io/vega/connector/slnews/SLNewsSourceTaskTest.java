package io.vega.connector.slnews;

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

class SLNewsSourceTaskTest {

    private SLNewsSourceTask task;
    private SLNewsRssClient rssClient;

    @BeforeEach
    void setUp() {
        task = new SLNewsSourceTask();
        task.setConfig(new SLNewsSourceConfig(Map.of(
                SLNewsSourceConfig.TOPIC_CONFIG, "raw-sl-news",
                SLNewsSourceConfig.POLL_INTERVAL_MS_CONFIG, "60000"
        )));
        rssClient = mock(SLNewsRssClient.class);
        task.setRssClient(rssClient);
        task.setLastPollMs(0L);
    }

    @Test
    void pollProducesOnlyNewArticles() throws Exception {
        SLNewsArticle a1 = sampleArticle("id-1", "Headline 1");
        SLNewsArticle a2 = sampleArticle("id-2", "Headline 2");

        when(rssClient.fetchArticles())
                .thenReturn(List.of(a1, a2))
                .thenReturn(List.of(a1, a2));

        assertEquals(2, task.poll().size());
        task.setLastPollMs(0L);
        assertTrue(task.poll().isEmpty());
        verify(rssClient, times(2)).fetchArticles();
    }

    @Test
    void deduplicationWithPreloadedOffsets() throws Exception {
        SLNewsArticle article = sampleArticle("id-1", "Headline");
        task.setSeenArticleIds(Set.of("id-1"));
        when(rssClient.fetchArticles()).thenReturn(List.of(article));

        assertTrue(task.poll().isEmpty());
    }

    private static SLNewsArticle sampleArticle(String id, String title) {
        SLNewsArticle article = new SLNewsArticle();
        article.setArticleId(id);
        article.setTitle(title);
        article.setLink("https://example.com/" + id);
        article.setSourceFeed("https://feed.test/rss");
        article.setSourceName("Test Source");
        article.setPublishedAt(1_700_000_000_000L);
        article.setIngestedAt(1_700_000_000_000L);
        article.setLanguage("en");
        return article;
    }
}
