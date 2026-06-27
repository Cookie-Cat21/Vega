package io.vega.flink.jobs;

import io.vega.flink.FlinkEnvFactory;
import io.vega.flink.kafka.KafkaAvroMappers;
import io.vega.flink.models.EditAnomaly;
import io.vega.flink.models.RawWikiEvent;
import io.vega.flink.operators.AnomalyDetector;
import io.vega.flink.operators.EditEnricher;
import io.vega.flink.sinks.IcebergSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class AnomalyDetectionJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();

        KafkaSource<RawWikiEvent> source = KafkaSource.<RawWikiEvent>builder()
                .setBootstrapServers(FlinkEnvFactory.kafkaBootstrapServers())
                .setTopics("raw-wiki-events")
                .setGroupId("vega-anomaly-detection")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setDeserializer(KafkaAvroMappers.wikiEventDeserializer(FlinkEnvFactory.schemaRegistryUrl()))
                .build();

        DataStream<RawWikiEvent> wikiStream = env
                .fromSource(source, WatermarkStrategy.<RawWikiEvent>forMonotonousTimestamps()
                        .withTimestampAssigner((event, ts) -> event.timestamp()), "wiki-kafka-source")
                .filter(e -> "edit".equals(e.type()));

        DataStream<EditAnomaly> anomalies = wikiStream
                .keyBy(RawWikiEvent::user)
                .process(new RapidEditDetector())
                .name("rapid-edit-detector");

        DataStream<EditAnomaly> largeEdits = wikiStream
                .flatMap(new LargeEditFlatMap())
                .name("large-edit-detector");

        DataStream<EditAnomaly> allAnomalies = anomalies.union(largeEdits);

        IcebergSinkFactory.writeToIceberg(allAnomalies, "vega", "edit_anomalies", EditAnomaly.class);

        env.execute("AnomalyDetectionJob");
    }

    static class LargeEditFlatMap implements FlatMapFunction<RawWikiEvent, EditAnomaly> {
        @Override
        public void flatMap(RawWikiEvent event, org.apache.flink.util.Collector<EditAnomaly> out) {
            AnomalyDetector.detectLargeEdit(event).forEach(out::collect);
        }
    }

    static class RapidEditDetector extends KeyedProcessFunction<String, RawWikiEvent, EditAnomaly> {
        private transient ListState<RawWikiEvent> recentEdits;

        @Override
        public void open(Configuration parameters) {
            recentEdits = getRuntimeContext().getListState(
                    new ListStateDescriptor<>("recent-edits", Types.POJO(RawWikiEvent.class)));
        }

        @Override
        public void processElement(RawWikiEvent event, Context ctx, Collector<EditAnomaly> out) throws Exception {
            List<RawWikiEvent> history = new ArrayList<>();
            for (RawWikiEvent e : recentEdits.get()) {
                history.add(e);
            }
            history.add(event);
            history.removeIf(e -> e.timestamp() < event.timestamp() - AnomalyDetector.RAPID_EDIT_WINDOW_MS);
            recentEdits.update(history);

            AnomalyDetector.detectRapidEdits(event.user(), history, event.timestamp()).forEach(out::collect);
        }
    }
}
