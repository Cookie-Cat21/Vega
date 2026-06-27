-- Vega lakehouse table: enriched Wikipedia edit events
CREATE TABLE IF NOT EXISTS vega.wiki_events_enriched (
    id BIGINT COMMENT 'Wikipedia revision ID',
    title STRING COMMENT 'Article title',
    user STRING COMMENT 'Editor username',
    bot BOOLEAN COMMENT 'Whether edit was made by a bot',
    wiki STRING COMMENT 'Source wiki (e.g. enwiki)',
    server_url STRING COMMENT 'Wikipedia server URL',
    timestamp TIMESTAMP COMMENT 'Edit timestamp',
    type STRING COMMENT 'Event type (edit)',
    namespace INT COMMENT 'MediaWiki namespace',
    comment STRING COMMENT 'Edit summary',
    length_old INT COMMENT 'Article length before edit',
    length_new INT COMMENT 'Article length after edit',
    revision_old BIGINT COMMENT 'Previous revision ID',
    revision_new BIGINT COMMENT 'New revision ID',
    edit_size_delta INT COMMENT 'Character count change',
    language_group STRING COMMENT 'Language code extracted from wiki',
    is_new_article BOOLEAN COMMENT 'True if article was newly created'
)
USING iceberg
PARTITIONED BY (days(timestamp))
TBLPROPERTIES (
    'format-version' = '2',
    'write.upsert.enabled' = 'false',
    'comment' = 'Enriched Wikipedia edit stream from Vega Flink WikiEnrichmentJob'
);
