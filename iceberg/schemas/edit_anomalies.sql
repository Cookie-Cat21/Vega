-- Vega lakehouse table: detected Wikipedia edit anomalies
CREATE TABLE IF NOT EXISTS vega.edit_anomalies (
    title STRING COMMENT 'Article title',
    user STRING COMMENT 'Editor username',
    wiki STRING COMMENT 'Source wiki',
    timestamp TIMESTAMP COMMENT 'Anomaly detection timestamp',
    anomaly_type STRING COMMENT 'LARGE_EDIT or RAPID_EDITS',
    edit_size_delta INT COMMENT 'Edit size in characters',
    edit_count INT COMMENT 'Number of edits in rapid-edit window'
)
USING iceberg
PARTITIONED BY (days(timestamp))
TBLPROPERTIES (
    'format-version' = '2',
    'comment' = 'Wikipedia edit anomalies from Vega Flink AnomalyDetectionJob'
);
