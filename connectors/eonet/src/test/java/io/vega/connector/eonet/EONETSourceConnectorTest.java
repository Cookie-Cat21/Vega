package io.vega.connector.eonet;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EONETSourceConnectorTest {

    @Test
    void taskClassReturnsEonetSourceTask() {
        assertEquals(EONETSourceTask.class, new EONETSourceConnector().taskClass());
    }

    @Test
    void versionIsNotNull() {
        assertNotNull(new EONETSourceConnector().version());
    }

    @Test
    void taskConfigsReturnsConnectorConfig() {
        EONETSourceConnector connector = new EONETSourceConnector();
        Map<String, String> props = Map.of(EONETSourceConfig.TOPIC_CONFIG, "raw-natural-events");
        connector.start(props);

        List<Map<String, String>> taskConfigs = connector.taskConfigs(1);
        assertEquals(1, taskConfigs.size());
        assertEquals("raw-natural-events", taskConfigs.getFirst().get(EONETSourceConfig.TOPIC_CONFIG));
    }
}
