-- Vega lakehouse table: enriched natural disaster events from NASA EONET
CREATE TABLE IF NOT EXISTS vega.natural_events (
    event_id STRING COMMENT 'NASA EONET event identifier',
    title STRING COMMENT 'Event title',
    description STRING COMMENT 'Event description',
    category STRING COMMENT 'Event category (Wildfires, Earthquakes, etc.)',
    source_url STRING COMMENT 'Reference source URL',
    latitude DOUBLE COMMENT 'Event latitude',
    longitude DOUBLE COMMENT 'Event longitude',
    event_date TIMESTAMP COMMENT 'Event occurrence timestamp',
    magnitude_value DOUBLE COMMENT 'Magnitude value if applicable',
    magnitude_unit STRING COMMENT 'Magnitude unit (M, etc.)',
    is_closed BOOLEAN COMMENT 'Whether event is closed',
    ingested_at TIMESTAMP COMMENT 'Pipeline ingestion timestamp',
    region_name STRING COMMENT 'Geographic region from static lookup',
    severity_label STRING COMMENT 'Derived severity label'
)
USING iceberg
PARTITIONED BY (category)
TBLPROPERTIES (
    'format-version' = '2',
    'comment' = 'Enriched NASA EONET natural events from Vega Flink EONETEnrichmentJob'
);
