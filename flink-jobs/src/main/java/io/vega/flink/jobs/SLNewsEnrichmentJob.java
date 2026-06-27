package io.vega.flink.jobs;

import io.vega.flink.FlinkEnvFactory;
import io.vega.flink.kafka.KafkaAvroMappers;
import io.vega.flink.models.EnrichedSLNewsArticle;
import io.vega.flink.models.RawSLNewsArticle;
import io.vega.flink.operators.NewsEnricher;
import io.vega.flink.sinks.IcebergSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SLNewsEnrichmentJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();

        KafkaSource<RawSLNewsArticle> source = KafkaSource.<RawSLNewsArticle>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-sl-news")
                .setGroupId("vega-slnews-enrichment")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.slNewsDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        DataStream<EnrichedSLNewsArticle> enriched = env
                .fromSource(source, WatermarkStrategy.<RawSLNewsArticle>forMonotonousTimestamps()
                        .withTimestampAssigner((event, ts) -> event.publishedAt()), "slnews-kafka-source")
                .map(NewsEnricher::enrich)
                .name("news-enricher");

        IcebergSinkFactory.writeToIceberg(enriched, "vega", "sl_news_enriched", EnrichedSLNewsArticle.class);

        env.execute("SLNewsEnrichmentJob");
    }
}
