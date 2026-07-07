package io.vega.flink.sinks;

import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.sink.FlinkSink;
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
        boolean icebergEnabled = "true".equalsIgnoreCase(
                System.getenv().getOrDefault("VEGA_ICEBERG_ENABLED", "false"));

        if (icebergEnabled) {
            writeToIcebergTable(stream, database, table, recordClass);
        } else {
            LOG.debug("Iceberg disabled, writing to local file sink for table {}", table);
            writeToFile(stream, table);
        }
    }

    private static <T> void writeToIcebergTable(
            DataStream<T> stream,
            String database,
            String table,
            Class<T> recordClass
    ) {
        String catalogName = System.getenv().getOrDefault("ICEBERG_CATALOG_NAME", "vega_catalog");
        String warehouse = System.getenv().getOrDefault("ICEBERG_WAREHOUSE_PATH", "/tmp/iceberg/warehouse");

        LOG.info("Iceberg sink enabled: catalog={}, warehouse={}, table={}.{}",
                catalogName, warehouse, database, table);

        IcebergTableEnsurer.ensureTable(database, table);

        TableLoader tableLoader = IcebergCatalogFactory.tableLoader(database, table);

        DataStream<RowData> rows = stream
                .map(IcebergRowConverters.converterFor(recordClass))
                .name("iceberg-row-mapper-" + table);

        FlinkSink.forRowData(rows)
                .tableLoader(tableLoader)
                .uidPrefix("iceberg-sink-" + table)
                .append();
    }

    static <T> void writeToFile(DataStream<T> stream, String table) {
        FileSink<T> sink = FileSink
                .forRowFormat(new Path("/tmp/vega-output/" + table), new SimpleStringEncoder<T>("UTF-8"))
                .withRollingPolicy(org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies
                        .DefaultRollingPolicy.builder()
                        .withRolloverInterval(Duration.ofMinutes(5))
                        .build())
                .build();
        stream.sinkTo(sink).name("file-sink-" + table);
    }
}
