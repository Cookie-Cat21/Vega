package io.vega.flink;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlinkEnvFactoryParallelismTest {

    @Test
    void parallelismDefaultsToTwo() {
        assertEquals(2, FlinkEnvFactory.parallelism());
    }
}
