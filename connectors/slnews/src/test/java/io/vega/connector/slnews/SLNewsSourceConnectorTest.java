package io.vega.connector.slnews;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SLNewsSourceConnectorTest {

    @Test
    void taskClassReturnsSlNewsSourceTask() {
        assertEquals(SLNewsSourceTask.class, new SLNewsSourceConnector().taskClass());
    }

    @Test
    void versionIsNotNull() {
        assertNotNull(new SLNewsSourceConnector().version());
    }

    @Test
    void taskConfigsReturnsConnectorConfig() {
        SLNewsSourceConnector connector = new SLNewsSourceConnector();
        connector.start(Map.of(SLNewsSourceConfig.TOPIC_CONFIG, "raw-sl-news"));

        List<Map<String, String>> taskConfigs = connector.taskConfigs(1);
        assertEquals(1, taskConfigs.size());
        assertEquals("raw-sl-news", taskConfigs.getFirst().get(SLNewsSourceConfig.TOPIC_CONFIG));
    }
}
