package io.vega.flink.metrics;

import org.apache.flink.streaming.api.operators.StreamMap;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountingMapperTest {

    @Test
    void mapTransformsValue() throws Exception {
        TestCountingMapper mapper = new TestCountingMapper();

        try (OneInputStreamOperatorTestHarness<String, String> harness =
                     new OneInputStreamOperatorTestHarness<>(new StreamMap<>(mapper))) {
            harness.setup();
            harness.open();
            harness.processElement(new StreamRecord<>("hello", 0));

            StreamRecord<String> output = (StreamRecord<String>) harness.getOutput().iterator().next();
            assertEquals("HELLO", output.getValue());
        }
    }

    @Test
    void metricNameIsExposed() {
        TestCountingMapper mapper = new TestCountingMapper();
        assertEquals("test_processed_total", mapper.metricName());
    }

    private static final class TestCountingMapper extends CountingMapper<String, String> {

        @Override
        protected String mapRecord(String value) {
            return value.toUpperCase();
        }

        @Override
        protected String metricName() {
            return "test_processed_total";
        }
    }
}
