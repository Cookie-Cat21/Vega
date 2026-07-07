package io.vega.flink.metrics;

import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;

public abstract class CountingFilter<T> extends RichFilterFunction<T> {

    private transient Counter filteredCounter;

    protected abstract boolean matches(T value);

    protected abstract String metricName();

    @Override
    public void open(Configuration parameters) {
        MetricGroup group = getRuntimeContext().getMetricGroup().addGroup("vega");
        filteredCounter = group.counter(metricName());
    }

    @Override
    public boolean filter(T value) {
        boolean result = matches(value);
        if (result) {
            filteredCounter.inc();
        }
        return result;
    }
}
