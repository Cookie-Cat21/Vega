package io.vega.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public final class FlinkEnvFactory {

    private FlinkEnvFactory() {}

    public static StreamExecutionEnvironment create() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(60_000L);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30_000L);
        env.getCheckpointConfig().setCheckpointTimeout(120_000L);

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 3);
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofSeconds(10));
        env.configure(config);

        return env;
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
}
