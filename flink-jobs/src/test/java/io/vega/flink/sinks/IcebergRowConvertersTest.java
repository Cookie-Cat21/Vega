package io.vega.flink.sinks;

import io.vega.flink.models.EnrichedWikiEvent;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IcebergRowConvertersTest {

    @Test
    void convertsEnrichedWikiEventToRowData() {
        EnrichedWikiEvent event = new EnrichedWikiEvent(
                1L, "Earth", "alice", false, "enwiki", "https://en.wikipedia.org",
                1_700_000_000_000L, "edit", 0, "fix", 100, 250, 1L, 2L,
                150, "en", false);

        RowData row = IcebergRowConverters.toRow(event);

        assertEquals(17, row.getArity());
        assertEquals(1L, row.getLong(0));
        assertEquals(StringData.fromString("Earth"), row.getString(1));
        assertEquals(TimestampData.fromEpochMillis(1_700_000_000_000L), row.getTimestamp(6, 3));
        assertEquals(150, row.getInt(14));
    }

    @Test
    void converterForEnrichedWikiEventClass() {
        assertNotNull(IcebergRowConverters.converterFor(EnrichedWikiEvent.class));
    }

    @Test
    void schemaForWikiEventsTable() {
        assertEquals(17, IcebergTableSchemas.schemaFor("wiki_events_enriched").columns().size());
    }

    @Test
    void partitionSpecForWikiEventsUsesDayPartition() {
        var schema = IcebergTableSchemas.schemaFor("wiki_events_enriched");
        var spec = IcebergTableSchemas.partitionSpecFor("wiki_events_enriched", schema);
        assertEquals(1, spec.fields().size());
        assertTrue(spec.fields().getFirst().name().contains("timestamp"));
    }
}
