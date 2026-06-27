package io.vega.flink.operators;

import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.RawWikiEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditEnricherTest {

    @Test
    void enrichesEditSizeDelta() {
        RawWikiEvent raw = new RawWikiEvent(
                1L, "Earth", "alice", false, "enwiki", "https://en.wikipedia.org",
                1_700_000_000_000L, "edit", 0, "fix", 100, 250, 1L, 2L);

        EnrichedWikiEvent enriched = EditEnricher.enrich(raw);

        assertEquals(150, enriched.editSizeDelta());
        assertEquals("en", enriched.languageGroup());
        assertFalse(enriched.isNewArticle());
    }

    @Test
    void detectsNewArticle() {
        RawWikiEvent raw = new RawWikiEvent(
                1L, "New Page", "bob", false, "dewiki", "https://de.wikipedia.org",
                1_700_000_000_000L, "edit", 0, "create", null, 500, null, 1L);

        EnrichedWikiEvent enriched = EditEnricher.enrich(raw);

        assertTrue(enriched.isNewArticle());
        assertEquals("de", enriched.languageGroup());
    }

    @Test
    void handlesNullLengths() {
        RawWikiEvent raw = new RawWikiEvent(
                1L, "Page", "user", true, "frwiki", "https://fr.wikipedia.org",
                1_700_000_000_000L, "edit", 0, null, null, null, null, null);

        EnrichedWikiEvent enriched = EditEnricher.enrich(raw);

        assertEquals(0, enriched.editSizeDelta());
        assertTrue(enriched.isNewArticle());
    }
}
