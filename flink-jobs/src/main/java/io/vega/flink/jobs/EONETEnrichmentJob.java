package io.vega.flink.jobs;

import io.vega.flink.FlinkEnvFactory;
import io.vega.flink.kafka.KafkaAvroMappers;
import io.vega.flink.models.EnrichedNaturalEvent;
import io.vega.flink.models.NaturalEvent;
import io.vega.flink.operators.GeoEnricher;
import io.vega.flink.sinks.IcebergSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class EONETEnrichmentJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();

        KafkaSource<NaturalEvent> source = KafkaSource.<NaturalEvent>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-natural-events")
                .setGroupId("vega-eonet-enrichment")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.naturalEventDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        DataStream<EnrichedNaturalEvent> enriched = env
                .fromSource(source, WatermarkStrategy.<NaturalEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, ts) -> event.eventDate()), "eonet-kafka-source")
                .map(GeoEnricher::enrich)
                .name("geo-enricher");

        IcebergSinkFactory.writeToIceberg(enriched, "vega", "natural_events", EnrichedNaturalEvent.class);

        env.execute("EONETEnrichmentJob");
    }
}
