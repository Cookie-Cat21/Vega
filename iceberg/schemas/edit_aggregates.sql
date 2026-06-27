-- Vega lakehouse table: per-wiki edit aggregates (1-minute windows)
CREATE TABLE IF NOT EXISTS vega.edit_aggregates (
    wiki STRING COMMENT 'Source wiki identifier',
    window_start TIMESTAMP COMMENT 'Aggregation window start',
    window_end TIMESTAMP COMMENT 'Aggregation window end',
    total_edits BIGINT COMMENT 'Total edits in window',
    bot_edits BIGINT COMMENT 'Bot edits in window',
    human_edits BIGINT COMMENT 'Human edits in window',
    avg_edit_size DOUBLE COMMENT 'Average absolute edit size delta'
)
USING iceberg
PARTITIONED BY (days(window_start))
TBLPROPERTIES (
    'format-version' = '2',
    'comment' = 'Per-wiki edit aggregates from Vega Flink AggregationJob'
);
