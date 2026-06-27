package io.vega.flink.operators;

import io.vega.flink.models.EventCorrelation;
import io.vega.flink.models.NaturalEvent;
import io.vega.flink.models.RawWikiEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelationMatcherTest {

    @Test
    void extractsKeywordsFromTitle() {
        List<String> keywords = CorrelationMatcher.extractKeywords("Wildfire in California");
        assertTrue(keywords.contains("wildfire"));
        assertTrue(keywords.contains("california"));
    }

    @Test
    void matchesWikiTitleToNaturalEvent() {
        NaturalEvent natural = new NaturalEvent(
                "E1", "Wildfire in California", null, "Wildfires", null,
                38.0, -120.0, 1_700_000_000_000L, null, null, false, 1_700_000_000_000L);
        RawWikiEvent wiki = new RawWikiEvent(
                1L, "2024 California wildfires", "editor", false, "enwiki", "url",
                1_700_000_060_000L, "edit", 0, null, 100, 200, null, null);

        assertTrue(CorrelationMatcher.matches(wiki, natural));
    }

    @Test
    void correlatesEventsWithReactionTime() {
        NaturalEvent natural = new NaturalEvent(
                "E1", "Wildfire in California", null, "Wildfires", null,
                38.0, -120.0, 1_700_000_000_000L, null, null, false, 1_700_000_000_000L);
        RawWikiEvent wiki1 = new RawWikiEvent(
                1L, "California wildfire", "a", false, "enwiki", "url",
                1_700_000_030_000L, "edit", 0, null, 100, 200, null, null);
        RawWikiEvent wiki2 = new RawWikiEvent(
                2L, "California wildfire updates", "b", false, "enwiki", "url",
                1_700_000_090_000L, "edit", 0, null, 100, 200, null, null);

        EventCorrelation correlation = CorrelationMatcher.correlate(
                natural, List.of(wiki1, wiki2), 1_700_000_000_000L, 1_700_001_800_000L);

        assertEquals(2, correlation.editCount());
        assertEquals(30L, correlation.reactionTimeSeconds());
    }

    @Test
    void correlateBatchFindsMatches() {
        NaturalEvent natural = new NaturalEvent(
                "E1", "Hurricane Milton", null, "Severe Storms", null,
                25.0, -80.0, 1_700_000_000_000L, 3.0, "M", false, 1_700_000_000_000L);
        RawWikiEvent wiki = new RawWikiEvent(
                1L, "Hurricane Milton (2024)", "editor", false, "enwiki", "url",
                1_700_000_120_000L, "edit", 0, null, 100, 200, null, null);

        List<EventCorrelation> results = CorrelationMatcher.correlateBatch(
                List.of(natural), List.of(wiki), 1_700_000_000_000L, 1_700_001_800_000L);

        assertFalse(results.isEmpty());
        assertEquals(120L, results.getFirst().reactionTimeSeconds());
    }
}
