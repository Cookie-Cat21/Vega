package io.vega.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public final class FlinkEnvFactory {

    private FlinkEnvFactory() {}

    public static StreamExecutionEnvironment create() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        long checkpointIntervalMs = checkpointIntervalMs();
        env.enableCheckpointing(checkpointIntervalMs);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(Math.min(30_000L, checkpointIntervalMs / 2));
        env.getCheckpointConfig().setCheckpointTimeout(Math.max(120_000L, checkpointIntervalMs * 4));

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 3);
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofSeconds(10));
        env.configure(config);

        return env;
    }

    public static long checkpointIntervalMs() {
        return Long.parseLong(System.getenv().getOrDefault("VEGA_CHECKPOINT_INTERVAL_MS", "60000"));
    }

    public static String kafkaBootstrapServers() {
        return System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    }

    public static String schemaRegistryUrl() {
        return System.getenv().getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8082");
    }

    public static String icebergWarehousePath() {
        return System.getenv().getOrDefault("ICEBERG_WAREHOUSE_PATH", "/tmp/iceberg/warehouse");
    }

    public static String icebergCatalogName() {
        return System.getenv().getOrDefault("ICEBERG_CATALOG_NAME", "vega_catalog");
    }

    public static int parallelism() {
        return Integer.parseInt(System.getenv().getOrDefault("VEGA_FLINK_PARALLELISM", "2"));
    }

    /**
     * Kafka source start position. Use {@code earliest} for local demos that produce
     * fixtures around job submit; default {@code latest} matches streaming ingest.
     */
    public static OffsetsInitializer kafkaStartingOffsets() {
        String mode = System.getenv().getOrDefault("VEGA_KAFKA_STARTING_OFFSETS", "latest");
        if ("earliest".equalsIgnoreCase(mode)) {
            return OffsetsInitializer.earliest();
        }
        return OffsetsInitializer.latest();
    }

    public static String consumerGroup(String base) {
        String suffix = System.getenv().getOrDefault("VEGA_CONSUMER_GROUP_SUFFIX", "");
        return suffix.isBlank() ? base : base + "-" + suffix;
    }
}
