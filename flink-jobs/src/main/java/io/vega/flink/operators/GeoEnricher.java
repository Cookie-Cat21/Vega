package io.vega.flink.operators;

import io.vega.flink.models.EnrichedNaturalEvent;
import io.vega.flink.models.NaturalEvent;

public final class GeoEnricher {

    private GeoEnricher() {}

    public static EnrichedNaturalEvent enrich(NaturalEvent event) {
        return new EnrichedNaturalEvent(
                event.eventId(),
                event.title(),
                event.description(),
                event.category(),
                event.sourceUrl(),
                event.latitude(),
                event.longitude(),
                event.eventDate(),
                event.magnitudeValue(),
                event.magnitudeUnit(),
                event.isClosed(),
                event.ingestedAt(),
                resolveRegion(event.latitude(), event.longitude()),
                resolveSeverity(event.category(), event.magnitudeValue())
        );
    }

    static String resolveRegion(double latitude, double longitude) {
        if (latitude >= -35 && latitude <= -10 && longitude >= 110 && longitude <= 155) {
            return "Australia";
        }
        if (latitude >= 25 && latitude <= 50 && longitude >= -125 && longitude <= -65) {
            return "North America";
        }
        if (latitude >= -55 && latitude <= 15 && longitude >= -82 && longitude <= -34) {
            return "South America";
        }
        if (latitude >= 35 && latitude <= 72 && longitude >= -10 && longitude <= 40) {
            return "Europe";
        }
        if (latitude >= -35 && latitude <= 37 && longitude >= -18 && longitude <= 52) {
            return "Africa";
        }
        if (latitude >= 5 && latitude <= 55 && longitude >= 60 && longitude <= 150) {
            return "Asia";
        }
        if (latitude >= -50 && latitude <= 0 && longitude >= 110 && longitude <= 180) {
            return "Oceania";
        }
        return "Unknown";
    }

    static String resolveSeverity(String category, Double magnitudeValue) {
        if (category == null) {
            return "Unknown";
        }
        return switch (category) {
            case "Wildfires", "Volcanoes" -> magnitudeValue == null ? "Active" : "Moderate";
            case "Severe Storms" -> {
                if (magnitudeValue == null) {
                    yield "Moderate";
                }
                if (magnitudeValue < 2.0) {
                    yield "Minor";
                } else if (magnitudeValue < 4.0) {
                    yield "Moderate";
                } else {
                    yield "Severe";
                }
            }
            case "Earthquakes" -> {
                if (magnitudeValue == null) {
                    yield "Unknown";
                }
                if (magnitudeValue < 4.0) {
                    yield "Minor";
                } else if (magnitudeValue < 6.0) {
                    yield "Moderate";
                } else {
                    yield "Severe";
                }
            }
            default -> magnitudeValue == null ? "Active" : "Moderate";
        };
    }
}
