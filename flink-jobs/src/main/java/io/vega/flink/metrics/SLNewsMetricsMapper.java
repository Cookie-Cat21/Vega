package io.vega.flink.metrics;

import io.vega.flink.models.EnrichedSLNewsArticle;
import io.vega.flink.models.RawSLNewsArticle;
import io.vega.flink.operators.NewsEnricher;

public final class SLNewsMetricsMapper extends CountingMapper<RawSLNewsArticle, EnrichedSLNewsArticle> {

    @Override
    protected EnrichedSLNewsArticle mapRecord(RawSLNewsArticle value) {
        return NewsEnricher.enrich(value);
    }

    @Override
    protected String metricName() {
        return "sl_news_articles_total";
    }
}
