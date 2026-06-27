-- Vega lakehouse table: correlations between natural events and Wikipedia edits
CREATE TABLE IF NOT EXISTS vega.event_correlations (
    natural_event_id STRING COMMENT 'NASA EONET event ID',
    natural_event_title STRING COMMENT 'Natural event title',
    category STRING COMMENT 'Event category',
    wiki_article_title STRING COMMENT 'Top correlated Wikipedia article',
    edit_count BIGINT COMMENT 'Number of correlated edits',
    first_edit_timestamp TIMESTAMP COMMENT 'Timestamp of first correlated edit',
    reaction_time_seconds BIGINT COMMENT 'Seconds from event to first edit',
    window_start TIMESTAMP COMMENT 'Correlation window start',
    window_end TIMESTAMP COMMENT 'Correlation window end'
)
USING iceberg
PARTITIONED BY (days(window_start))
TBLPROPERTIES (
    'format-version' = '2',
    'comment' = 'Natural event to Wikipedia reaction correlations from Vega CorrelationJob'
);
