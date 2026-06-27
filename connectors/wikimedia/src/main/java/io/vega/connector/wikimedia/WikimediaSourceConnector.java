package io.vega.connector.wikimedia;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WikimediaSourceConnector extends SourceConnector {

    private Map<String, String> configProps;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        this.configProps = props;
        new WikimediaSourceConfig(props);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return WikimediaSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        return Collections.nCopies(maxTasks, configProps);
    }

    @Override
    public void stop() {
        // no-op
    }

    @Override
    public ConfigDef config() {
        return WikimediaSourceConfig.CONFIG_DEF;
    }
}
