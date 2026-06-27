package io.vega.flink.jobs;

import io.vega.flink.FlinkEnvFactory;
import io.vega.flink.kafka.KafkaAvroMappers;
import io.vega.flink.models.EditAggregate;
import io.vega.flink.models.EnrichedWikiEvent;
import io.vega.flink.models.RawWikiEvent;
import io.vega.flink.operators.EditEnricher;
import io.vega.flink.sinks.IcebergSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

public class AggregationJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();

        KafkaSource<RawWikiEvent> source = KafkaSource.<RawWikiEvent>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-wiki-events")
                .setGroupId("vega-aggregation")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.wikiEventDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        DataStream<EditAggregate> aggregates = env
                .fromSource(source, WatermarkStrategy.<RawWikiEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, ts) -> event.timestamp()), "wiki-kafka-source")
                .filter(e -> "edit".equals(e.type()))
                .map(EditEnricher::enrich)
                .keyBy(EnrichedWikiEvent::wiki)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .aggregate(new EditAggregateFunction())
                .name("wiki-aggregation");

        IcebergSinkFactory.writeToIceberg(aggregates, "vega", "edit_aggregates", EditAggregate.class);

        env.execute("AggregationJob");
    }

    static class EditAggregateFunction implements AggregateFunction<EnrichedWikiEvent, Acc, EditAggregate> {

        @Override
        public Acc createAccumulator() {
            return new Acc();
        }

        @Override
        public Acc add(EnrichedWikiEvent event, Acc acc) {
            if (acc.wiki == null) {
                acc.wiki = event.wiki();
                acc.windowStart = event.timestamp();
            }
            acc.windowEnd = event.timestamp();
            acc.totalEdits++;
            if (event.bot()) {
                acc.botEdits++;
            } else {
                acc.humanEdits++;
            }
            acc.totalEditSize += Math.abs(event.editSizeDelta());
            return acc;
        }

        @Override
        public EditAggregate getResult(Acc acc) {
            double avg = acc.totalEdits == 0 ? 0.0 : (double) acc.totalEditSize / acc.totalEdits;
            return new EditAggregate(
                    acc.wiki,
                    acc.windowStart,
                    acc.windowEnd,
                    acc.totalEdits,
                    acc.botEdits,
                    acc.humanEdits,
                    avg
            );
        }

        @Override
        public Acc merge(Acc a, Acc b) {
            a.totalEdits += b.totalEdits;
            a.botEdits += b.botEdits;
            a.humanEdits += b.humanEdits;
            a.totalEditSize += b.totalEditSize;
            if (b.windowEnd > a.windowEnd) {
                a.windowEnd = b.windowEnd;
            }
            return a;
        }
    }

    static class Acc {
        String wiki;
        long windowStart;
        long windowEnd;
        long totalEdits;
        long botEdits;
        long humanEdits;
        long totalEditSize;
    }
}
