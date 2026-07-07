package io.vega.flink.jobs;

import io.vega.flink.models.EditAggregate;
import io.vega.flink.models.EnrichedWikiEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationJobTest {

    @Test
    void aggregateFunctionComputesAverages() {
        AggregationJob.EditAggregateFunction fn = new AggregationJob.EditAggregateFunction();
        AggregationJob.Acc acc = fn.createAccumulator();

        EnrichedWikiEvent botEdit = enrichedEvent(true, 50);
        EnrichedWikiEvent humanEdit = enrichedEvent(false, 100);

        fn.add(botEdit, acc);
        fn.add(humanEdit, acc);

        EditAggregate result = fn.getResult(acc);

        assertEquals(2, result.totalEdits());
        assertEquals(1, result.botEdits());
        assertEquals(1, result.humanEdits());
        assertEquals(75.0, result.avgEditSize());
    }

    private static EnrichedWikiEvent enrichedEvent(boolean bot, int delta) {
        return new EnrichedWikiEvent(
                1L, "Page", "user", bot, "enwiki", "https://en.wikipedia.org",
                1_700_000_000_000L, "edit", 0, "c", 0, delta, 1L, 2L,
                delta, "en", false);
    }
}
