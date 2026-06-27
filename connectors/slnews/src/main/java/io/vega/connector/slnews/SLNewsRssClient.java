package io.vega.connector.slnews;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class SLNewsRssClient {

    private static final Logger LOG = LoggerFactory.getLogger(SLNewsRssClient.class);
    private static final long MAX_BACKOFF_MS = 30_000L;

    private final List<String> feedUrls;
    private final HttpClient httpClient;

    public SLNewsRssClient(List<String> feedUrls) {
        this.feedUrls = feedUrls;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()))
                .build();
    }

    public List<SLNewsArticle> fetchArticles() throws IOException, InterruptedException {
        List<SLNewsArticle> articles = new ArrayList<>();
        long ingestedAt = System.currentTimeMillis();

        for (String feedUrl : feedUrls) {
            try {
                articles.addAll(fetchFeed(feedUrl, ingestedAt));
            } catch (Exception e) {
                LOG.warn("Failed to fetch RSS feed {}: {}", feedUrl, e.getMessage());
            }
        }
        return articles;
    }

    List<SLNewsArticle> fetchFeed(String feedUrl, long ingestedAt) throws IOException, InterruptedException {
        String body = fetchWithRetry(feedUrl);
        return parseFeed(body, feedUrl, ingestedAt);
    }

    private String fetchWithRetry(String feedUrl) throws IOException, InterruptedException {
        long backoffMs = 1_000L;
        while (true) {
            try {
                return doRequest(feedUrl);
            } catch (IOException e) {
                LOG.warn("RSS fetch failed for {}, retrying in {}ms: {}", feedUrl, backoffMs, e.getMessage());
                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
            }
        }
    }

    private String doRequest(String feedUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(feedUrl))
                .header("Accept", "application/rss+xml, application/xml, text/xml")
                .header("User-Agent", "VegaPipeline/1.0 (https://github.com/Cookie-Cat21/Vega)")
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 500) {
            throw new IOException("RSS feed returned status " + response.statusCode());
        }
        if (response.statusCode() != 200) {
            throw new IOException("RSS feed returned unexpected status " + response.statusCode());
        }
        return response.body();
    }

    List<SLNewsArticle> parseFeed(String xml, String feedUrl, long ingestedAt) throws IOException {
        List<SLNewsArticle> articles = new ArrayList<>();
        SyndFeedInput input = new SyndFeedInput();

        try (InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
             XmlReader reader = new XmlReader(stream)) {
            SyndFeed feed = input.build(reader);
            String sourceName = feed.getTitle() != null ? feed.getTitle() : extractSourceName(feedUrl);

            for (SyndEntry entry : feed.getEntries()) {
                articles.add(toArticle(entry, feedUrl, sourceName, ingestedAt));
            }
        } catch (Exception e) {
            throw new IOException("Failed to parse RSS from " + feedUrl + ": " + e.getMessage(), e);
        }
        return articles;
    }

    static SLNewsArticle toArticle(SyndEntry entry, String feedUrl, String sourceName, long ingestedAt) {
        String articleId = entry.getUri() != null ? entry.getUri()
                : entry.getLink() != null ? entry.getLink()
                : entry.getTitle();

        String description = null;
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            description = stripHtml(entry.getDescription().getValue());
        }

        Date published = entry.getPublishedDate() != null ? entry.getPublishedDate() : entry.getUpdatedDate();
        long publishedAt = published != null ? published.getTime() : ingestedAt;

        SLNewsArticle article = new SLNewsArticle();
        article.setArticleId(articleId);
        article.setTitle(entry.getTitle() != null ? entry.getTitle() : "");
        article.setDescription(description);
        article.setLink(entry.getLink() != null ? entry.getLink() : articleId);
        article.setSourceFeed(feedUrl);
        article.setSourceName(sourceName);
        article.setPublishedAt(publishedAt);
        article.setIngestedAt(ingestedAt);
        article.setLanguage(detectLanguage(article.getTitle().toString(), description));
        return article;
    }

    static String extractSourceName(String feedUrl) {
        try {
            String host = URI.create(feedUrl).getHost();
            if (host != null && host.startsWith("www.")) {
                return host.substring(4);
            }
            return host != null ? host : feedUrl;
        } catch (Exception e) {
            return feedUrl;
        }
    }

    static String stripHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    static String detectLanguage(String title, String description) {
        String text = (title != null ? title : "") + " " + (description != null ? description : "");
        if (text.matches(".*[\\u0D80-\\u0DFF].*")) {
            return "si";
        }
        return "en";
    }
}
