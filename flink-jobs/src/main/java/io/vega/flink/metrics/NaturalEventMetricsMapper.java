package io.vega.flink.metrics;

import io.vega.flink.models.EnrichedNaturalEvent;
import io.vega.flink.models.NaturalEvent;
import io.vega.flink.operators.GeoEnricher;

public final class NaturalEventMetricsMapper extends CountingMapper<NaturalEvent, EnrichedNaturalEvent> {

    @Override
    protected EnrichedNaturalEvent mapRecord(NaturalEvent value) {
        return GeoEnricher.enrich(value);
    }

    @Override
    protected String metricName() {
        return "natural_events_total";
    }
}
