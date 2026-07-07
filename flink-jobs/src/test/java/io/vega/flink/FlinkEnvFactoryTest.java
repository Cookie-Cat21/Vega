package io.vega.flink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlinkEnvFactoryTest {

    @Test
    void createReturnsConfiguredEnvironment() {
        StreamExecutionEnvironment env = FlinkEnvFactory.create();
        assertNotNull(env);
        assertEquals(60_000L, env.getCheckpointConfig().getCheckpointInterval());
    }

    @Test
    void kafkaBootstrapServersDefaultsToLocalhost() {
        assertEquals("localhost:9092", FlinkEnvFactory.kafkaBootstrapServers());
    }

    @Test
    void schemaRegistryUrlDefaultsToLocalhost() {
        assertEquals("http://localhost:8082", FlinkEnvFactory.schemaRegistryUrl());
    }

    @Test
    void icebergWarehousePathDefaultsToTmp() {
        assertEquals("/tmp/iceberg/warehouse", FlinkEnvFactory.icebergWarehousePath());
    }

    @Test
    void icebergCatalogNameDefaultsToVegaCatalog() {
        assertEquals("vega_catalog", FlinkEnvFactory.icebergCatalogName());
    }
}
