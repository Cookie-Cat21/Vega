package io.vega.flink.sinks;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.time.Duration;

public final class IcebergSinkFactory {

    private IcebergSinkFactory() {}

    public static <T> void writeToIceberg(
            DataStream<T> stream,
            String database,
            String table,
            Class<T> recordClass
    ) {
        String warehouse = System.getenv().getOrDefault("ICEBERG_WAREHOUSE_PATH", "/tmp/iceberg/warehouse");
        boolean icebergEnabled = "true".equalsIgnoreCase(
                System.getenv().getOrDefault("VEGA_ICEBERG_ENABLED", "false"));

        if (icebergEnabled) {
            writeToIcebergTable(stream, database, table, warehouse);
        } else {
            writeToFile(stream, table);
        }
    }

    private static <T> void writeToIcebergTable(
            DataStream<T> stream,
            String database,
            String table,
            String warehouse
    ) {
        stream.map(Object::toString)
                .sinkTo(FileSink
                        .forRowFormat(
                                new Path(warehouse + "/" + database + "/" + table),
                                new SimpleStringEncoder<String>("UTF-8"))
                        .withRollingPolicy(DefaultRollingPolicy.builder()
                                .withRolloverInterval(Duration.ofMinutes(5))
                                .build())
                        .build())
                .name("iceberg-sink-" + table);
    }

    static <T> void writeToFile(DataStream<T> stream, String table) {
        FileSink<T> sink = FileSink
                .forRowFormat(new Path("/tmp/vega-output/" + table), new SimpleStringEncoder<T>("UTF-8"))
                .withRollingPolicy(DefaultRollingPolicy.builder()
                        .withRolloverInterval(Duration.ofMinutes(5))
                        .build())
                .build();
        stream.sinkTo(sink).name("file-sink-" + table);
    }
}
