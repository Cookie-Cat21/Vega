package io.vega.connector.eonet;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EONETSourceConfigTest {

    @Test
    void defaultsAreApplied() {
        EONETSourceConfig config = new EONETSourceConfig(Map.of());

        assertEquals("raw-natural-events", config.topic());
        assertEquals("https://eonet.gsfc.nasa.gov/api/v3/events", config.apiUrl());
        assertEquals(60_000L, config.pollIntervalMs());
        assertEquals(30, config.daysLookback());
        assertEquals("open", config.status());
    }

    @Test
    void customValuesAreAccepted() {
        EONETSourceConfig config = new EONETSourceConfig(Map.of(
                EONETSourceConfig.TOPIC_CONFIG, "custom-events",
                EONETSourceConfig.API_URL_CONFIG, "http://localhost/eonet",
                EONETSourceConfig.POLL_INTERVAL_MS_CONFIG, "30000",
                EONETSourceConfig.DAYS_LOOKBACK_CONFIG, "7",
                EONETSourceConfig.STATUS_CONFIG, "closed"
        ));

        assertEquals("custom-events", config.topic());
        assertEquals("http://localhost/eonet", config.apiUrl());
        assertEquals(30_000L, config.pollIntervalMs());
        assertEquals(7, config.daysLookback());
        assertEquals("closed", config.status());
    }

    @Test
    void rejectsInvalidStatus() {
        assertThrows(Exception.class, () -> new EONETSourceConfig(Map.of(
                EONETSourceConfig.STATUS_CONFIG, "invalid"
        )));
    }
}
