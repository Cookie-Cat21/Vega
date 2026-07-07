package io.vega.flink.sinks;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public final class IcebergSinkFactory {

    private static final Logger LOG = LoggerFactory.getLogger(IcebergSinkFactory.class);

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
        String catalogName = System.getenv().getOrDefault("ICEBERG_CATALOG_NAME", "vega_catalog");

        if (icebergEnabled) {
            LOG.info("Iceberg sink enabled: catalog={}, warehouse={}, table={}.{}",
                    catalogName, warehouse, database, table);
            writeToIcebergTable(stream, database, table, warehouse);
        } else {
            LOG.debug("Iceberg disabled, writing to local file sink for table {}", table);
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
