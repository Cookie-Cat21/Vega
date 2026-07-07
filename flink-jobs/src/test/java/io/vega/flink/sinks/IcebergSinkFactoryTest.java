package io.vega.flink.sinks;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class IcebergSinkFactoryTest {

    @Test
    void writeToFileSinkDoesNotThrow() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.fromData("test-record");

        assertDoesNotThrow(() ->
                IcebergSinkFactory.writeToIceberg(stream, "vega", "test_table", String.class));
    }
}
