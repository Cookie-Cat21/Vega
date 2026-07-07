package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditAggregateTest {

    @Test
    void recordHoldsAggregateFields() {
        EditAggregate agg = new EditAggregate(
                "enwiki", 1_700_000_000_000L, 1_700_000_060_000L,
                100, 20, 80, 42.5);

        assertEquals("enwiki", agg.wiki());
        assertEquals(100, agg.totalEdits());
        assertEquals(42.5, agg.avgEditSize());
    }
}
