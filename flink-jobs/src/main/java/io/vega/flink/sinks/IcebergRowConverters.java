package io.vega.flink.sinks;

import io.vega.flink.models.EditAggregate;
import io.vega.flink.models.EditAnomaly;
import io.vega.flink.models.EnrichedNaturalEvent;
import io.vega.flink.models.EnrichedSLNewsArticle;
import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.EventCorrelation;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;

import java.time.Instant;

final class IcebergRowConverters {

    private IcebergRowConverters() {}

    @SuppressWarnings("unchecked")
    static <T> MapFunction<T, RowData> converterFor(Class<T> recordClass) {
        if (recordClass == EnrichedWikiEvent.class) {
            return event -> (RowData) toRow((EnrichedWikiEvent) event);
        }
        if (recordClass == EnrichedNaturalEvent.class) {
            return event -> (RowData) toRow((EnrichedNaturalEvent) event);
        }
        if (recordClass == EventCorrelation.class) {
            return event -> (RowData) toRow((EventCorrelation) event);
        }
        if (recordClass == EditAnomaly.class) {
            return event -> (RowData) toRow((EditAnomaly) event);
        }
        if (recordClass == EditAggregate.class) {
            return event -> (RowData) toRow((EditAggregate) event);
        }
        if (recordClass == EnrichedSLNewsArticle.class) {
            return event -> (RowData) toRow((EnrichedSLNewsArticle) event);
        }
        throw new IllegalArgumentException("No Iceberg row converter for " + recordClass.getName());
    }

    static RowData toRow(EnrichedWikiEvent event) {
        GenericRowData row = new GenericRowData(17);
        row.setField(0, event.id());
        row.setField(1, string(event.title()));
        row.setField(2, string(event.user()));
        row.setField(3, event.bot());
        row.setField(4, string(event.wiki()));
        row.setField(5, string(event.serverUrl()));
        row.setField(6, timestamp(event.timestamp()));
        row.setField(7, string(event.type()));
        row.setField(8, event.namespace());
        row.setField(9, string(event.comment()));
        row.setField(10, event.lengthOld());
        row.setField(11, event.lengthNew());
        row.setField(12, event.revisionOld());
        row.setField(13, event.revisionNew());
        row.setField(14, event.editSizeDelta());
        row.setField(15, string(event.languageGroup()));
        row.setField(16, event.isNewArticle());
        return row;
    }

    static RowData toRow(EnrichedNaturalEvent event) {
        GenericRowData row = new GenericRowData(14);
        row.setField(0, string(event.eventId()));
        row.setField(1, string(event.title()));
        row.setField(2, string(event.description()));
        row.setField(3, string(event.category()));
        row.setField(4, string(event.sourceUrl()));
        row.setField(5, event.latitude());
        row.setField(6, event.longitude());
        row.setField(7, timestamp(event.eventDate()));
        row.setField(8, event.magnitudeValue());
        row.setField(9, string(event.magnitudeUnit()));
        row.setField(10, event.isClosed());
        row.setField(11, timestamp(event.ingestedAt()));
        row.setField(12, string(event.regionName()));
        row.setField(13, string(event.severityLabel()));
        return row;
    }

    static RowData toRow(EventCorrelation event) {
        GenericRowData row = new GenericRowData(9);
        row.setField(0, string(event.naturalEventId()));
        row.setField(1, string(event.naturalEventTitle()));
        row.setField(2, string(event.category()));
        row.setField(3, string(event.wikiArticleTitle()));
        row.setField(4, event.editCount());
        row.setField(5, timestamp(event.firstEditTimestamp()));
        row.setField(6, event.reactionTimeSeconds());
        row.setField(7, timestamp(event.windowStart()));
        row.setField(8, timestamp(event.windowEnd()));
        return row;
    }

    static RowData toRow(EditAnomaly event) {
        GenericRowData row = new GenericRowData(7);
        row.setField(0, string(event.title()));
        row.setField(1, string(event.user()));
        row.setField(2, string(event.wiki()));
        row.setField(3, timestamp(event.timestamp()));
        row.setField(4, string(event.anomalyType().name()));
        row.setField(5, event.editSizeDelta());
        row.setField(6, event.editCount());
        return row;
    }

    static RowData toRow(EditAggregate event) {
        GenericRowData row = new GenericRowData(7);
        row.setField(0, string(event.wiki()));
        row.setField(1, timestamp(event.windowStart()));
        row.setField(2, timestamp(event.windowEnd()));
        row.setField(3, event.totalEdits());
        row.setField(4, event.botEdits());
        row.setField(5, event.humanEdits());
        row.setField(6, event.avgEditSize());
        return row;
    }

    static RowData toRow(EnrichedSLNewsArticle event) {
        GenericRowData row = new GenericRowData(12);
        row.setField(0, string(event.articleId()));
        row.setField(1, string(event.title()));
        row.setField(2, string(event.description()));
        row.setField(3, string(event.link()));
        row.setField(4, string(event.sourceFeed()));
        row.setField(5, string(event.sourceName()));
        row.setField(6, timestamp(event.publishedAt()));
        row.setField(7, timestamp(event.ingestedAt()));
        row.setField(8, string(event.language()));
        row.setField(9, string(event.category()));
        row.setField(10, event.wordCount());
        row.setField(11, event.isBreaking());
        return row;
    }

    private static StringData string(String value) {
        return value == null ? null : StringData.fromString(value);
    }

    private static TimestampData timestamp(long epochMillis) {
        return TimestampData.fromInstant(Instant.ofEpochMilli(epochMillis));
    }
}
