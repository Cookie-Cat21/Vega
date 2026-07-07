package io.vega.flink.jobs;

import io.vega.flink.models.EditAggregate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationJobMergeTest {

    @Test
    void mergeCombinesAccumulators() {
        AggregationJob.EditAggregateFunction fn = new AggregationJob.EditAggregateFunction();

        AggregationJob.Acc a = fn.createAccumulator();
        a.wiki = "enwiki";
        a.windowStart = 1_000L;
        a.windowEnd = 2_000L;
        a.totalEdits = 3;
        a.botEdits = 1;
        a.humanEdits = 2;
        a.totalEditSize = 300;

        AggregationJob.Acc b = new AggregationJob.Acc();
        b.totalEdits = 2;
        b.botEdits = 0;
        b.humanEdits = 2;
        b.totalEditSize = 200;
        b.windowEnd = 3_000L;

        AggregationJob.Acc merged = fn.merge(a, b);
        EditAggregate result = fn.getResult(merged);

        assertEquals(5, result.totalEdits());
        assertEquals(1, result.botEdits());
        assertEquals(4, result.humanEdits());
        assertEquals(100.0, result.avgEditSize());
        assertEquals(3_000L, result.windowEnd());
        assertEquals("enwiki", result.wiki());
    }

    @Test
    void mergePreservesEarlierWindowStart() {
        AggregationJob.EditAggregateFunction fn = new AggregationJob.EditAggregateFunction();

        AggregationJob.Acc a = fn.createAccumulator();
        a.wiki = "dewiki";
        a.windowStart = 5_000L;
        a.windowEnd = 6_000L;
        a.totalEdits = 1;
        a.totalEditSize = 10;

        AggregationJob.Acc b = new AggregationJob.Acc();
        b.windowStart = 7_000L;
        b.windowEnd = 8_000L;
        b.totalEdits = 1;
        b.totalEditSize = 20;

        AggregationJob.Acc merged = fn.merge(a, b);

        assertEquals(5_000L, merged.windowStart);
        assertEquals(8_000L, merged.windowEnd);
    }
}
