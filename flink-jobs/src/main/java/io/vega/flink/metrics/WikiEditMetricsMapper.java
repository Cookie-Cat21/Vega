package io.vega.flink.metrics;

import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.RawWikiEvent;
import io.vega.flink.operators.EditEnricher;

public final class WikiEditMetricsMapper extends CountingMapper<RawWikiEvent, EnrichedWikiEvent> {

    @Override
    protected EnrichedWikiEvent mapRecord(RawWikiEvent value) {
        return EditEnricher.enrich(value);
    }

    @Override
    protected String metricName() {
        return "wiki_edits_total";
    }
}
