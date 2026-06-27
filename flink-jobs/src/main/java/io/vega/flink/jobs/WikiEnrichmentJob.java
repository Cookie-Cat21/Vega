package io.vega.flink.jobs;

import io.vega.flink.FlinkEnvFactory;
import io.vega.flink.kafka.KafkaAvroMappers;
import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.RawWikiEvent;
import io.vega.flink.operators.EditEnricher;
import io.vega.flink.sinks.IcebergSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class WikiEnrichmentJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();

        KafkaSource<RawWikiEvent> source = KafkaSource.<RawWikiEvent>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-wiki-events")
                .setGroupId("vega-wiki-enrichment")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.wikiEventDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        DataStream<EnrichedWikiEvent> enriched = env
                .fromSource(source, WatermarkStrategy.<RawWikiEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, ts) -> event.timestamp()), "wiki-kafka-source")
                .filter(e -> "edit".equals(e.type()))
                .map(EditEnricher::enrich)
                .name("edit-enricher");

        IcebergSinkFactory.writeToIceberg(enriched, "vega", "wiki_events_enriched", EnrichedWikiEvent.class);

        env.execute("WikiEnrichmentJob");
    }
}
