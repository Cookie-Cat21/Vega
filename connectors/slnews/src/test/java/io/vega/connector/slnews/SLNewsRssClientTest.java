package io.vega.connector.slnews;

import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SLNewsRssClientTest {

    private static final String SAMPLE_RSS = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Ada Derana</title>
                <item>
                  <title>Breaking: Flood warnings issued</title>
                  <link>https://example.com/flood</link>
                  <guid>https://example.com/flood</guid>
                  <pubDate>Sat, 27 Jun 2026 10:00:00 GMT</pubDate>
                  <description><![CDATA[<p>Heavy rains expected</p>]]></description>
                </item>
                <item>
                  <title>කොළඹ තත්වය</title>
                  <link>https://example.com/si</link>
                  <guid>si-article-1</guid>
                  <pubDate>Sat, 27 Jun 2026 09:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
            """;

    @Test
    void parseFeedExtractsArticles() throws Exception {
        SLNewsRssClient client = new SLNewsRssClient(java.util.List.of("http://localhost"));
        var articles = client.parseFeed(SAMPLE_RSS, "https://www.adaderana.lk/rss.php", 1_700_000_000_000L);

        assertEquals(2, articles.size());
        assertEquals("Breaking: Flood warnings issued", articles.get(0).getTitle());
        assertEquals("Ada Derana", articles.get(0).getSourceName());
        assertEquals("en", articles.get(0).getLanguage());
        assertEquals("si", articles.get(1).getLanguage());
    }

    @Test
    void stripHtmlRemovesTags() {
        assertEquals("Heavy rains expected", SLNewsRssClient.stripHtml("<p>Heavy rains expected</p>"));
    }

    @Test
    void detectLanguageIdentifiesSinhala() {
        assertEquals("si", SLNewsRssClient.detectLanguage("කොළඹ", null));
        assertEquals("en", SLNewsRssClient.detectLanguage("Colombo news", null));
    }

    @Test
    void toArticleMapsFields() {
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle("Test headline");
        entry.setLink("https://example.com/a");
        entry.setUri("article-123");
        entry.setPublishedDate(new Date(1_700_000_000_000L));
        SyndContentImpl desc = new SyndContentImpl();
        desc.setValue("<b>Summary</b>");
        entry.setDescription(desc);

        SLNewsArticle article = SLNewsRssClient.toArticle(
                entry, "https://feed.test/rss", "Test Source", 1_700_000_100_000L);

        assertEquals("article-123", article.getArticleId());
        assertEquals("Test headline", article.getTitle());
        assertEquals("Summary", article.getDescription().toString());
        assertFalse(article.getDescription().toString().contains("<"));
    }
}
