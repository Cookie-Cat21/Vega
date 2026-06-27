package io.vega.flink.operators;

import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.RawWikiEvent;

public final class EditEnricher {

    private EditEnricher() {}

    public static EnrichedWikiEvent enrich(RawWikiEvent event) {
        int oldLen = event.lengthOld() != null ? event.lengthOld() : 0;
        int newLen = event.lengthNew() != null ? event.lengthNew() : 0;
        int editSizeDelta = newLen - oldLen;
        String languageGroup = extractLanguageGroup(event.wiki());
        boolean isNewArticle = event.lengthOld() == null || event.lengthOld() == 0;

        return new EnrichedWikiEvent(
                event.id(),
                event.title(),
                event.user(),
                event.bot(),
                event.wiki(),
                event.serverUrl(),
                event.timestamp(),
                event.type(),
                event.namespace(),
                event.comment(),
                event.lengthOld(),
                event.lengthNew(),
                event.revisionOld(),
                event.revisionNew(),
                editSizeDelta,
                languageGroup,
                isNewArticle
        );
    }

    static String extractLanguageGroup(String wiki) {
        if (wiki == null || wiki.isBlank()) {
            return "unknown";
        }
        if (wiki.endsWith("wiki")) {
            return wiki.substring(0, wiki.length() - 4);
        }
        return wiki;
    }
}
