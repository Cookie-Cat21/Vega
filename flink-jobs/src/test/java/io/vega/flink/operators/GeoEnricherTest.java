package io.vega.flink.operators;

import io.vega.flink.models.EnrichedNaturalEvent;
import io.vega.flink.models.NaturalEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeoEnricherTest {

    @Test
    void mapsAustraliaRegion() {
        NaturalEvent event = sampleEvent(-25.0, 130.0, "Wildfires", null);
        EnrichedNaturalEvent enriched = GeoEnricher.enrich(event);
        assertEquals("Australia", enriched.regionName());
        assertEquals("Active", enriched.severityLabel());
    }

    @Test
    void mapsNorthAmericaRegion() {
        NaturalEvent event = sampleEvent(40.0, -100.0, "Wildfires", null);
        assertEquals("North America", GeoEnricher.enrich(event).regionName());
    }

    @Test
    void mapsSevereStormMagnitude() {
        NaturalEvent minor = sampleEvent(30.0, -90.0, "Severe Storms", 1.5);
        NaturalEvent severe = sampleEvent(30.0, -90.0, "Severe Storms", 5.0);

        assertEquals("Minor", GeoEnricher.enrich(minor).severityLabel());
        assertEquals("Severe", GeoEnricher.enrich(severe).severityLabel());
    }

    @Test
    void mapsEarthquakeSeverity() {
        NaturalEvent event = sampleEvent(35.0, 139.0, "Earthquakes", 6.5);
        assertEquals("Severe", GeoEnricher.enrich(event).severityLabel());
        assertEquals("Asia", GeoEnricher.enrich(event).regionName());
    }

    private static NaturalEvent sampleEvent(double lat, double lon, String category, Double magnitude) {
        return new NaturalEvent(
                "E1", "Test Event", null, category, null,
                lat, lon, 1_700_000_000_000L, magnitude, magnitude != null ? "M" : null,
                false, System.currentTimeMillis());
    }
}
