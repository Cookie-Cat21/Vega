package io.vega.connector.eonet;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EONETRestClientTest {

    private static final String SAMPLE_RESPONSE = """
            {
              "title": "EONET Events",
              "events": [
                {
                  "id": "EONET_1234",
                  "title": "Wildfire in California",
                  "description": "Active wildfire",
                  "categories": [{"id": "wildfires", "title": "Wildfires"}],
                  "sources": [{"id": "NOAA", "url": "https://example.com/fire"}],
                  "closed": null,
                  "geometry": [
                    {"magnitudeValue": null, "date": "2024-06-01T12:00:00Z", "type": "Point", "coordinates": [-120.5, 38.5]},
                    {"magnitudeValue": null, "date": "2024-06-01T14:00:00Z", "type": "Point", "coordinates": [-121.0, 39.0]}
                  ]
                },
                {
                  "id": "EONET_5678",
                  "title": "Earthquake in Japan",
                  "categories": [{"id": "earthquakes", "title": "Earthquakes"}],
                  "magnitudeValue": 5.2,
                  "magnitudeUnit": "M",
                  "closed": "2024-06-02T00:00:00Z",
                  "geometry": [
                    {"date": "2024-06-01T08:00:00Z", "type": "Point", "coordinates": [139.7, 35.6]}
                  ]
                }
              ]
            }
            """;

    @Test
    void parseResponseCreatesRecordPerGeometry() throws Exception {
        EONETRestClient client = new EONETRestClient("http://localhost", 30, "open");
        List<NaturalEvent> events = client.parseResponse(SAMPLE_RESPONSE);

        assertEquals(3, events.size());
        assertEquals("EONET_1234", events.get(0).getEventId());
        assertEquals("Wildfires", events.get(0).getCategory());
        assertEquals(38.5, events.get(0).getLatitude());
        assertEquals(-120.5, events.get(0).getLongitude());
        assertEquals(false, events.get(0).getIsClosed());
    }

    @Test
    void parseResponseExtractsMagnitude() throws Exception {
        EONETRestClient client = new EONETRestClient("http://localhost", 30, "open");
        List<NaturalEvent> events = client.parseResponse(SAMPLE_RESPONSE);

        NaturalEvent earthquake = events.stream()
                .filter(e -> "EONET_5678".equals(e.getEventId()))
                .findFirst()
                .orElseThrow();

        assertEquals(5.2, earthquake.getMagnitudeValue());
        assertEquals("M", earthquake.getMagnitudeUnit());
        assertTrue(earthquake.getIsClosed());
    }

    @Test
    void parseDateMillisHandlesIsoFormat() {
        long millis = EONETRestClient.parseDateMillis("2024-06-01T12:00:00Z");
        assertTrue(millis > 0);
    }
}
