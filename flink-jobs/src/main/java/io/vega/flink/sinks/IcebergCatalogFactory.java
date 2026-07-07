package io.vega.flink.sinks;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.flink.CatalogLoader;
import org.apache.iceberg.flink.TableLoader;

import java.util.HashMap;
import java.util.Map;

public final class IcebergCatalogFactory {

    private IcebergCatalogFactory() {}

    public static CatalogLoader catalogLoader() {
        String catalogName = env("ICEBERG_CATALOG_NAME", "vega_catalog");
        String warehouse = env("ICEBERG_WAREHOUSE_PATH", "/tmp/iceberg/warehouse");
        Configuration hadoopConf = new Configuration();

        Map<String, String> properties = new HashMap<>();
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, warehouse);

        if (warehouse.startsWith("abfs://") || warehouse.startsWith("abfss://")) {
            configureAzureAuth(properties);
        }

        return CatalogLoader.hadoop(catalogName, hadoopConf, properties);
    }

    public static TableLoader tableLoader(String database, String table) {
        return TableLoader.fromCatalog(catalogLoader(), TableIdentifier.of(database, table));
    }

    private static void configureAzureAuth(Map<String, String> properties) {
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.azure.adlsv2.ADLSFileIO");
        properties.put("adls.auth.type", "oauth");

        String clientId = env("AZURE_CLIENT_ID", "");
        String clientSecret = env("AZURE_CLIENT_SECRET", "");
        String tenantId = env("AZURE_TENANT_ID", "");

        if (!clientId.isBlank()) {
            properties.put("adls.oauth2.client-id", clientId);
        }
        if (!clientSecret.isBlank()) {
            properties.put("adls.oauth2.client-secret", clientSecret);
        }
        if (!tenantId.isBlank()) {
            properties.put("adls.oauth2.tenant-id", tenantId);
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
