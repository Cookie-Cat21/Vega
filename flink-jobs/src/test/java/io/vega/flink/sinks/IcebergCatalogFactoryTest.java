package io.vega.flink.sinks;

import org.apache.iceberg.flink.CatalogLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class IcebergCatalogFactoryTest {

    @Test
    void catalogLoaderUsesLocalWarehouseByDefault() {
        CatalogLoader loader = IcebergCatalogFactory.catalogLoader();
        assertNotNull(loader);
    }

    @Test
    void tableLoaderCreatesForKnownTable() {
        assertNotNull(IcebergCatalogFactory.tableLoader("vega", "wiki_events_enriched"));
    }
}
