package io.vega.flink.metrics;

import io.vega.flink.models.EventCorrelation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class CorrelationMetricsSink extends RichSinkFunction<EventCorrelation> {

    private transient Counter correlationsCounter;

    @Override
    public void open(Configuration parameters) {
        correlationsCounter = getRuntimeContext()
                .getMetricGroup()
                .addGroup("vega")
                .counter("event_correlations_total");
    }

    @Override
    public void invoke(EventCorrelation value, Context context) {
        correlationsCounter.inc();
    }
}
