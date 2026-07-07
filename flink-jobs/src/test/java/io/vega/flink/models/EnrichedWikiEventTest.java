package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrichedWikiEventTest {

    @Test
    void recordHoldsEnrichmentFields() {
        EnrichedWikiEvent event = new EnrichedWikiEvent(
                1L, "Earth", "alice", false, "enwiki", "https://en.wikipedia.org",
                1_700_000_000_000L, "edit", 0, "fix", 100, 250, 1L, 2L,
                150, "en", false);

        assertEquals(150, event.editSizeDelta());
        assertEquals("en", event.languageGroup());
        assertTrue(!event.isNewArticle());
    }
}
