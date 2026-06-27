package io.vega.flink.operators;

import io.vega.flink.models.EventCorrelation;
import io.vega.flink.models.NaturalEvent;
import io.vega.flink.models.RawWikiEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class CorrelationMatcher {

    private static final Pattern WORD_SPLIT = Pattern.compile("[^a-z0-9]+");

    private CorrelationMatcher() {}

    public static List<String> extractKeywords(String title) {
        if (title == null || title.isBlank()) {
            return List.of();
        }
        return Arrays.stream(WORD_SPLIT.split(title.toLowerCase(Locale.ROOT)))
                .filter(word -> word.length() > 2)
                .distinct()
                .toList();
    }

    public static boolean matches(RawWikiEvent wikiEvent, NaturalEvent naturalEvent) {
        List<String> keywords = extractKeywords(naturalEvent.title());
        String titleLower = wikiEvent.title().toLowerCase(Locale.ROOT);
        return keywords.stream().anyMatch(titleLower::contains);
    }

    public static EventCorrelation correlate(
            NaturalEvent naturalEvent,
            List<RawWikiEvent> matchingEdits,
            long windowStart,
            long windowEnd
    ) {
        long firstEdit = matchingEdits.stream()
                .mapToLong(RawWikiEvent::timestamp)
                .min()
                .orElse(windowEnd);

        String topArticle = matchingEdits.stream()
                .map(RawWikiEvent::title)
                .findFirst()
                .orElse("");

        long reactionSeconds = Math.max(0, (firstEdit - naturalEvent.eventDate()) / 1000);

        return new EventCorrelation(
                naturalEvent.eventId(),
                naturalEvent.title(),
                naturalEvent.category(),
                topArticle,
                matchingEdits.size(),
                firstEdit,
                reactionSeconds,
                windowStart,
                windowEnd
        );
    }

    public static List<EventCorrelation> correlateBatch(
            List<NaturalEvent> naturalEvents,
            List<RawWikiEvent> wikiEvents,
            long windowStart,
            long windowEnd
    ) {
        List<EventCorrelation> results = new ArrayList<>();
        for (NaturalEvent naturalEvent : naturalEvents) {
            List<RawWikiEvent> matches = wikiEvents.stream()
                    .filter(w -> w.timestamp() >= windowStart && w.timestamp() <= windowEnd)
                    .filter(w -> matches(w, naturalEvent))
                    .toList();
            if (!matches.isEmpty()) {
                results.add(correlate(naturalEvent, matches, windowStart, windowEnd));
            }
        }
        return results;
    }
}
