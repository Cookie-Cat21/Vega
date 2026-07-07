package io.vega.flink.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikiEditMetricsMapperTest {

    @Test
    void metricNameIsWikiEditsTotal() {
        WikiEditMetricsMapper mapper = new WikiEditMetricsMapper();
        assertEquals("wiki_edits_total", mapper.metricName());
    }
}
