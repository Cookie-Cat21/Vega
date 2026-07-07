package io.vega.flink.metrics;

import org.apache.flink.streaming.api.operators.StreamFilter;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.util.OneInputStreamOperatorTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountingFilterTest {

    @Test
    void filterPassesMatchingValues() throws Exception {
        TestCountingFilter filter = new TestCountingFilter();

        try (OneInputStreamOperatorTestHarness<String, String> harness =
                     new OneInputStreamOperatorTestHarness<>(new StreamFilter<>(filter))) {
            harness.setup();
            harness.open();
            harness.processElement(new StreamRecord<>("keep", 0));
            harness.processElement(new StreamRecord<>("drop", 1));

            assertEquals(1, harness.getOutput().size());
            StreamRecord<String> output = (StreamRecord<String>) harness.getOutput().iterator().next();
            assertEquals("keep", output.getValue());
        }
    }

    @Test
    void metricNameIsExposed() {
        TestCountingFilter filter = new TestCountingFilter();
        assertEquals("test_filtered_total", filter.metricName());
    }

    @Test
    void matchesReturnsTrueForKeepPrefix() {
        TestCountingFilter filter = new TestCountingFilter();
        assertTrue(filter.matches("keep-me"));
    }

    private static final class TestCountingFilter extends CountingFilter<String> {

        @Override
        protected boolean matches(String value) {
            return value != null && value.startsWith("keep");
        }

        @Override
        protected String metricName() {
            return "test_filtered_total";
        }
    }
}
