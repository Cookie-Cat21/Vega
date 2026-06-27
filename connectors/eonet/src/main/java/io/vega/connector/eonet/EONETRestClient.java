package io.vega.connector.eonet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class EONETRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(EONETRestClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long MAX_BACKOFF_MS = 30_000L;

    private final String apiUrl;
    private final int daysLookback;
    private final String status;
    private final HttpClient httpClient;

    public EONETRestClient(String apiUrl, int daysLookback, String status) {
        this.apiUrl = apiUrl;
        this.daysLookback = daysLookback;
        this.status = status;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .executor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()))
                .build();
    }

    public List<NaturalEvent> fetchEvents() throws IOException, InterruptedException {
        return fetchEventsWithRetry(1_000L);
    }

    List<NaturalEvent> fetchEventsWithRetry(long initialBackoffMs) throws IOException, InterruptedException {
        long backoffMs = initialBackoffMs;
        while (true) {
            try {
                return parseResponse(doRequest());
            } catch (IOException e) {
                LOG.warn("EONET API request failed, retrying in {}ms: {}", backoffMs, e.getMessage());
                Thread.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
            }
        }
    }

    private String doRequest() throws IOException, InterruptedException {
        String url = apiUrl + "?status=" + URLEncoder.encode(status, StandardCharsets.UTF_8)
                + "&days=" + daysLookback;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", "VegaPipeline/1.0 (https://github.com/Cookie-Cat21/Vega)")
                .GET()
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 500) {
            throw new IOException("EONET API returned status " + response.statusCode());
        }
        if (response.statusCode() != 200) {
            throw new IOException("EONET API returned unexpected status " + response.statusCode());
        }

        return response.body();
    }

    List<NaturalEvent> parseResponse(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        JsonNode events = root.path("events");
        List<NaturalEvent> results = new ArrayList<>();
        long ingestedAt = System.currentTimeMillis();

        if (!events.isArray()) {
            return results;
        }

        for (JsonNode eventNode : events) {
            results.addAll(parseEvent(eventNode, ingestedAt));
        }
        return results;
    }

    private List<NaturalEvent> parseEvent(JsonNode eventNode, long ingestedAt) {
        List<NaturalEvent> records = new ArrayList<>();
        String eventId = eventNode.path("id").asText();
        String title = eventNode.path("title").asText();
        String description = nullableText(eventNode, "description");
        boolean isClosed = !eventNode.path("closed").isNull() && !eventNode.path("closed").isMissingNode();

        String category = "Unknown";
        JsonNode categories = eventNode.path("categories");
        if (categories.isArray() && !categories.isEmpty()) {
            category = categories.get(0).path("title").asText("Unknown");
        }

        String sourceUrl = null;
        JsonNode sources = eventNode.path("sources");
        if (sources.isArray() && !sources.isEmpty()) {
            sourceUrl = nullableText(sources.get(0), "url");
        }

        Double magnitudeValue = null;
        String magnitudeUnit = null;
        JsonNode magnitude = eventNode.path("magnitudeValue");
        if (!magnitude.isMissingNode() && !magnitude.isNull()) {
            magnitudeValue = magnitude.asDouble();
            magnitudeUnit = nullableText(eventNode, "magnitudeUnit");
        }

        JsonNode geometries = eventNode.path("geometry");
        if (!geometries.isArray()) {
            return records;
        }

        for (JsonNode geometry : geometries) {
            JsonNode coordinates = geometry.path("coordinates");
            if (!coordinates.isArray() || coordinates.size() < 2) {
                continue;
            }

            NaturalEvent record = new NaturalEvent();
            record.setEventId(eventId);
            record.setTitle(title);
            record.setDescription(description);
            record.setCategory(category);
            record.setSourceUrl(sourceUrl);
            record.setLongitude(coordinates.get(0).asDouble());
            record.setLatitude(coordinates.get(1).asDouble());
            record.setEventDate(parseDateMillis(geometry.path("date").asText(null)));
            record.setMagnitudeValue(magnitudeValue);
            record.setMagnitudeUnit(magnitudeUnit);
            record.setIsClosed(isClosed);
            record.setIngestedAt(ingestedAt);
            records.add(record);
        }

        return records;
    }

    static long parseDateMillis(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return System.currentTimeMillis();
        }
        try {
            return Instant.parse(dateText).toEpochMilli();
        } catch (DateTimeParseException e) {
            return System.currentTimeMillis();
        }
    }

    private static String nullableText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }
}
