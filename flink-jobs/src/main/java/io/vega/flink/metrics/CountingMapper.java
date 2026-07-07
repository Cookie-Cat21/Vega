package io.vega.flink.metrics;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.MetricGroup;

public abstract class CountingMapper<T, R> extends RichMapFunction<T, R> {

    private transient Counter processedCounter;

    protected abstract R mapRecord(T value);

    protected abstract String metricName();

    @Override
    public void open(Configuration parameters) {
        MetricGroup group = getRuntimeContext().getMetricGroup().addGroup("vega");
        processedCounter = group.counter(metricName());
    }

    @Override
    public R map(T value) {
        processedCounter.inc();
        return mapRecord(value);
    }
}
