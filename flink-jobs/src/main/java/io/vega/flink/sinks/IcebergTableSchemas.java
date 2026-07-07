package io.vega.flink.sinks;

import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;

final class IcebergTableSchemas {

    private IcebergTableSchemas() {}

    static Schema schemaFor(String table) {
        return switch (table) {
            case "wiki_events_enriched" -> wikiEventsEnriched();
            case "natural_events" -> naturalEvents();
            case "event_correlations" -> eventCorrelations();
            case "edit_anomalies" -> editAnomalies();
            case "edit_aggregates" -> editAggregates();
            case "sl_news_enriched" -> slNewsEnriched();
            default -> throw new IllegalArgumentException("Unknown Iceberg table: " + table);
        };
    }

    static PartitionSpec partitionSpecFor(String table, Schema schema) {
        return switch (table) {
            case "wiki_events_enriched" -> PartitionSpec.builderFor(schema).day("timestamp").build();
            case "natural_events" -> PartitionSpec.builderFor(schema).identity("category").build();
            case "event_correlations" -> PartitionSpec.builderFor(schema).day("window_start").build();
            case "edit_anomalies" -> PartitionSpec.builderFor(schema).day("timestamp").build();
            case "edit_aggregates" -> PartitionSpec.builderFor(schema).day("window_start").build();
            case "sl_news_enriched" -> PartitionSpec.builderFor(schema).day("published_at").build();
            default -> throw new IllegalArgumentException("Unknown Iceberg table: " + table);
        };
    }

    private static Schema wikiEventsEnriched() {
        return new Schema(
                Types.NestedField.optional(1, "id", Types.LongType.get()),
                Types.NestedField.required(2, "title", Types.StringType.get()),
                Types.NestedField.required(3, "user", Types.StringType.get()),
                Types.NestedField.required(4, "bot", Types.BooleanType.get()),
                Types.NestedField.required(5, "wiki", Types.StringType.get()),
                Types.NestedField.required(6, "server_url", Types.StringType.get()),
                Types.NestedField.required(7, "timestamp", Types.TimestampType.withZone()),
                Types.NestedField.required(8, "type", Types.StringType.get()),
                Types.NestedField.required(9, "namespace", Types.IntegerType.get()),
                Types.NestedField.optional(10, "comment", Types.StringType.get()),
                Types.NestedField.optional(11, "length_old", Types.IntegerType.get()),
                Types.NestedField.optional(12, "length_new", Types.IntegerType.get()),
                Types.NestedField.optional(13, "revision_old", Types.LongType.get()),
                Types.NestedField.optional(14, "revision_new", Types.LongType.get()),
                Types.NestedField.required(15, "edit_size_delta", Types.IntegerType.get()),
                Types.NestedField.required(16, "language_group", Types.StringType.get()),
                Types.NestedField.required(17, "is_new_article", Types.BooleanType.get())
        );
    }

    private static Schema naturalEvents() {
        return new Schema(
                Types.NestedField.required(1, "event_id", Types.StringType.get()),
                Types.NestedField.required(2, "title", Types.StringType.get()),
                Types.NestedField.optional(3, "description", Types.StringType.get()),
                Types.NestedField.required(4, "category", Types.StringType.get()),
                Types.NestedField.optional(5, "source_url", Types.StringType.get()),
                Types.NestedField.required(6, "latitude", Types.DoubleType.get()),
                Types.NestedField.required(7, "longitude", Types.DoubleType.get()),
                Types.NestedField.required(8, "event_date", Types.TimestampType.withZone()),
                Types.NestedField.optional(9, "magnitude_value", Types.DoubleType.get()),
                Types.NestedField.optional(10, "magnitude_unit", Types.StringType.get()),
                Types.NestedField.required(11, "is_closed", Types.BooleanType.get()),
                Types.NestedField.required(12, "ingested_at", Types.TimestampType.withZone()),
                Types.NestedField.optional(13, "region_name", Types.StringType.get()),
                Types.NestedField.optional(14, "severity_label", Types.StringType.get())
        );
    }

    private static Schema eventCorrelations() {
        return new Schema(
                Types.NestedField.required(1, "natural_event_id", Types.StringType.get()),
                Types.NestedField.required(2, "natural_event_title", Types.StringType.get()),
                Types.NestedField.required(3, "category", Types.StringType.get()),
                Types.NestedField.required(4, "wiki_article_title", Types.StringType.get()),
                Types.NestedField.required(5, "edit_count", Types.LongType.get()),
                Types.NestedField.required(6, "first_edit_timestamp", Types.TimestampType.withZone()),
                Types.NestedField.required(7, "reaction_time_seconds", Types.LongType.get()),
                Types.NestedField.required(8, "window_start", Types.TimestampType.withZone()),
                Types.NestedField.required(9, "window_end", Types.TimestampType.withZone())
        );
    }

    private static Schema editAnomalies() {
        return new Schema(
                Types.NestedField.required(1, "title", Types.StringType.get()),
                Types.NestedField.required(2, "user", Types.StringType.get()),
                Types.NestedField.required(3, "wiki", Types.StringType.get()),
                Types.NestedField.required(4, "timestamp", Types.TimestampType.withZone()),
                Types.NestedField.required(5, "anomaly_type", Types.StringType.get()),
                Types.NestedField.required(6, "edit_size_delta", Types.IntegerType.get()),
                Types.NestedField.required(7, "edit_count", Types.IntegerType.get())
        );
    }

    private static Schema editAggregates() {
        return new Schema(
                Types.NestedField.required(1, "wiki", Types.StringType.get()),
                Types.NestedField.required(2, "window_start", Types.TimestampType.withZone()),
                Types.NestedField.required(3, "window_end", Types.TimestampType.withZone()),
                Types.NestedField.required(4, "total_edits", Types.LongType.get()),
                Types.NestedField.required(5, "bot_edits", Types.LongType.get()),
                Types.NestedField.required(6, "human_edits", Types.LongType.get()),
                Types.NestedField.required(7, "avg_edit_size", Types.DoubleType.get())
        );
    }

    private static Schema slNewsEnriched() {
        return new Schema(
                Types.NestedField.required(1, "article_id", Types.StringType.get()),
                Types.NestedField.required(2, "title", Types.StringType.get()),
                Types.NestedField.optional(3, "description", Types.StringType.get()),
                Types.NestedField.required(4, "link", Types.StringType.get()),
                Types.NestedField.required(5, "source_feed", Types.StringType.get()),
                Types.NestedField.required(6, "source_name", Types.StringType.get()),
                Types.NestedField.required(7, "published_at", Types.TimestampType.withZone()),
                Types.NestedField.required(8, "ingested_at", Types.TimestampType.withZone()),
                Types.NestedField.required(9, "language", Types.StringType.get()),
                Types.NestedField.required(10, "category", Types.StringType.get()),
                Types.NestedField.required(11, "word_count", Types.IntegerType.get()),
                Types.NestedField.required(12, "is_breaking", Types.BooleanType.get())
        );
    }
}
