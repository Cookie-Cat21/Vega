package io.vega.flink.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SLNewsMetricsMapperTest {

    @Test
    void metricNameIsSlNewsArticlesTotal() {
        SLNewsMetricsMapper mapper = new SLNewsMetricsMapper();
        assertEquals("sl_news_articles_total", mapper.metricName());
    }
}
