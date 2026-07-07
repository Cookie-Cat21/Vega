package io.vega.flink.sinks;

import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class IcebergTableEnsurer {

    private static final Logger LOG = LoggerFactory.getLogger(IcebergTableEnsurer.class);

    private IcebergTableEnsurer() {}

    static void ensureTable(String database, String table) {
        CatalogLoader catalogLoader = IcebergCatalogFactory.catalogLoader();
        TableIdentifier tableId = TableIdentifier.of(database, table);
        Schema schema = IcebergTableSchemas.schemaFor(table);
        PartitionSpec spec = IcebergTableSchemas.partitionSpecFor(table, schema);

        try {
            Catalog catalog = catalogLoader.loadCatalog();
            if (!catalog.tableExists(tableId)) {
                catalog.createTable(tableId, schema, spec);
                LOG.info("Created Iceberg table {}.{}", database, table);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to ensure Iceberg table " + database + "." + table, e);
        }
    }
}
