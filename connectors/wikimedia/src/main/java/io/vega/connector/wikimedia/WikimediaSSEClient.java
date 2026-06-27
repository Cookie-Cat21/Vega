package io.vega.connector.wikimedia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class WikimediaSSEClient {

    private static final Logger LOG = LoggerFactory.getLogger(WikimediaSSEClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long MAX_BACKOFF_MS = 30_000L;

    private final String sseUrl;
    private final BlockingQueue<WikiEvent> eventQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final HttpClient httpClient;

    public WikimediaSSEClient(String sseUrl) {
        this(sseUrl, new LinkedBlockingQueue<>());
    }

    WikimediaSSEClient(String sseUrl, BlockingQueue<WikiEvent> eventQueue) {
        this.sseUrl = sseUrl;
        this.eventQueue = eventQueue;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .executor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()))
                .build();
    }

    public BlockingQueue<WikiEvent> getEventQueue() {
        return eventQueue;
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        Thread.ofVirtual().name("wikimedia-sse-consumer").start(this::consumeLoop);
        LOG.info("Started Wikimedia SSE client for {}", sseUrl);
    }

    public void stop() {
        running.set(false);
        LOG.info("Stopped Wikimedia SSE client");
    }

    public boolean isRunning() {
        return running.get();
    }

    private void consumeLoop() {
        long backoffMs = 1_000L;
        while (running.get()) {
            try {
                consumeStream();
                backoffMs = 1_000L;
            } catch (Exception e) {
                if (!running.get()) {
                    break;
                }
                LOG.warn("SSE connection error, reconnecting in {}ms: {}", backoffMs, e.getMessage());
                sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
            }
        }
    }

    private void consumeStream() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(sseUrl))
                .header("Accept", "text/event-stream")
                .header("User-Agent", "VegaPipeline/1.0 (https://github.com/Cookie-Cat21/Vega)")
                .GET()
                .timeout(Duration.ofHours(24))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("SSE endpoint returned status " + response.statusCode());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            String eventType = null;
            StringBuilder dataBuilder = new StringBuilder();

            while (running.get() && (line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    processEvent(eventType, dataBuilder.toString());
                    eventType = null;
                    dataBuilder.setLength(0);
                    continue;
                }

                if (line.startsWith("event:")) {
                    eventType = line.substring(6).trim();
                } else if (line.startsWith("data:")) {
                    if (!dataBuilder.isEmpty()) {
                        dataBuilder.append('\n');
                    }
                    dataBuilder.append(line.substring(5).trim());
                }
            }
        }
    }

    void processEvent(String eventType, String data) {
        if (data == null || data.isBlank()) {
            return;
        }
        if (eventType != null && !"message".equals(eventType)) {
            return;
        }
        try {
            WikiEvent event = parseEvent(data);
            if (event != null && "edit".equals(event.getType())) {
                eventQueue.offer(event);
            }
        } catch (Exception e) {
            LOG.debug("Failed to parse SSE event: {}", e.getMessage());
        }
    }

    WikiEvent parseEvent(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        String type = textOrEmpty(root, "type");
        if (!"edit".equals(type)) {
            return null;
        }

        WikiEvent event = new WikiEvent();
        event.setId(root.has("id") && !root.get("id").isNull() ? root.get("id").asLong() : null);
        event.setTitle(textOrEmpty(root, "title"));
        event.setUser(textOrEmpty(root, "user"));
        event.setBot(root.path("bot").asBoolean(false));
        event.setWiki(textOrEmpty(root, "wiki"));
        event.setServerUrl(textOrEmpty(root, "server_url"));
        event.setTimestamp(root.path("timestamp").asLong(0L) * 1000L);
        event.setType(type);
        event.setNamespace(root.path("namespace").asInt(0));
        event.setComment(nullableText(root, "comment"));

        JsonNode length = root.path("length");
        if (!length.isMissingNode() && !length.isNull()) {
            event.setLengthOld(length.has("old") && !length.get("old").isNull() ? length.get("old").asInt() : null);
            event.setLengthNew(length.has("new") && !length.get("new").isNull() ? length.get("new").asInt() : null);
        }

        JsonNode revision = root.path("revision");
        if (!revision.isMissingNode() && !revision.isNull()) {
            event.setRevisionOld(revision.has("old") && !revision.get("old").isNull() ? revision.get("old").asLong() : null);
            event.setRevisionNew(revision.has("new") && !revision.get("new").isNull() ? revision.get("new").asLong() : null);
        }

        return event;
    }

    private static String textOrEmpty(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : "";
    }

    private static String nullableText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
