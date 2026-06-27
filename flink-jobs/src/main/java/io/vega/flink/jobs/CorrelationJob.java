package io.vega.flink.jobs;

import io.vega.flink.FlinkEnvFactory;
import io.vega.flink.kafka.KafkaAvroMappers;
import io.vega.flink.models.EventCorrelation;
import io.vega.flink.models.NaturalEvent;
import io.vega.flink.models.RawWikiEvent;
import io.vega.flink.operators.CorrelationMatcher;
import io.vega.flink.sinks.IcebergSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class CorrelationJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();

        KafkaSource<NaturalEvent> naturalSource = KafkaSource.<NaturalEvent>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-natural-events")
                .setGroupId("vega-correlation-natural")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.naturalEventDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        KafkaSource<RawWikiEvent> wikiSource = KafkaSource.<RawWikiEvent>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-wiki-events")
                .setGroupId("vega-correlation-wiki")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.wikiEventDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        DataStream<NaturalEvent> naturalEvents = env
                .fromSource(naturalSource, WatermarkStrategy.<NaturalEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((e, ts) -> e.eventDate()), "natural-kafka-source");

        DataStream<RawWikiEvent> wikiEvents = env
                .fromSource(wikiSource, WatermarkStrategy.<RawWikiEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((e, ts) -> e.timestamp()), "wiki-kafka-source")
                .filter(e -> "edit".equals(e.type()));

        DataStream<EventCorrelation> correlations = naturalEvents
                .keyBy(NaturalEvent::eventId)
                .connect(wikiEvents.keyBy(e -> "all"))
                .process(new CorrelationCoProcessFunction())
                .name("event-correlator");

        IcebergSinkFactory.writeToIceberg(correlations, "vega", "event_correlations", EventCorrelation.class);

        env.execute("CorrelationJob");
    }

    static class CorrelationCoProcessFunction extends CoProcessFunction<NaturalEvent, RawWikiEvent, EventCorrelation> {
        private final List<NaturalEvent> bufferedNatural = new ArrayList<>();
        private final List<RawWikiEvent> bufferedWiki = new ArrayList<>();
        private long windowStart = Long.MAX_VALUE;
        private long windowEnd = 0L;
        private static final long WINDOW_MS = Time.minutes(30).toMilliseconds();

        @Override
        public void processElement1(NaturalEvent event, Context ctx, Collector<EventCorrelation> out) {
            bufferedNatural.add(event);
            updateWindow(event.eventDate());
            emitCorrelations(out);
        }

        @Override
        public void processElement2(RawWikiEvent event, Context ctx, Collector<EventCorrelation> out) {
            bufferedWiki.add(event);
            updateWindow(event.timestamp());
            emitCorrelations(out);
        }

        private void updateWindow(long timestamp) {
            if (windowStart == Long.MAX_VALUE) {
                windowStart = timestamp;
            }
            windowStart = Math.min(windowStart, timestamp);
            windowEnd = Math.max(windowEnd, timestamp);
        }

        private void emitCorrelations(Collector<EventCorrelation> out) {
            if (windowEnd - windowStart < WINDOW_MS / 10) {
                return;
            }
            long effectiveEnd = windowStart + WINDOW_MS;
            CorrelationMatcher.correlateBatch(bufferedNatural, bufferedWiki, windowStart, effectiveEnd)
                    .forEach(out::collect);
            bufferedNatural.clear();
            bufferedWiki.clear();
            windowStart = Long.MAX_VALUE;
            windowEnd = 0L;
        }
    }
}
