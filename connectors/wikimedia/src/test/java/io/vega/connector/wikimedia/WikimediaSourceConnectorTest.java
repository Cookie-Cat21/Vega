package io.vega.connector.wikimedia;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WikimediaSourceConnectorTest {

    @Test
    void taskClassReturnsWikimediaSourceTask() {
        WikimediaSourceConnector connector = new WikimediaSourceConnector();
        assertEquals(WikimediaSourceTask.class, connector.taskClass());
    }

    @Test
    void versionIsNotNull() {
        WikimediaSourceConnector connector = new WikimediaSourceConnector();
        assertNotNull(connector.version());
    }

    @Test
    void taskConfigsReturnsConnectorConfig() {
        WikimediaSourceConnector connector = new WikimediaSourceConnector();
        Map<String, String> props = Map.of(WikimediaSourceConfig.TOPIC_CONFIG, "raw-wiki-events");
        connector.start(props);

        List<Map<String, String>> taskConfigs = connector.taskConfigs(1);
        assertEquals(1, taskConfigs.size());
        assertEquals("raw-wiki-events", taskConfigs.getFirst().get(WikimediaSourceConfig.TOPIC_CONFIG));
    }

    @Test
    void configDefIsExposed() {
        WikimediaSourceConnector connector = new WikimediaSourceConnector();
        assertNotNull(connector.config());
    }

    @Test
    void taskClassIsWikimediaSourceTask() {
        assertEquals(WikimediaSourceTask.class, new WikimediaSourceConnector().taskClass());
    }
}
