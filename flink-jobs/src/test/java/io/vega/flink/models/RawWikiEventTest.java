package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RawWikiEventTest {

    @Test
    void recordHoldsAllFields() {
        RawWikiEvent event = new RawWikiEvent(
                42L, "Earth", "alice", false, "enwiki", "https://en.wikipedia.org",
                1_700_000_000_000L, "edit", 0, "fix typo", 100, 200, 1L, 2L);

        assertEquals(42L, event.id());
        assertEquals("Earth", event.title());
        assertEquals("alice", event.user());
        assertFalse(event.bot());
        assertEquals("enwiki", event.wiki());
        assertEquals("edit", event.type());
        assertEquals(100, event.lengthOld());
    }
}
