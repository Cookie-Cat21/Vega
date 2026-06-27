-- Vega lakehouse table: enriched Sri Lanka news articles (Lanka Lens)
CREATE TABLE IF NOT EXISTS vega.sl_news_enriched (
    article_id STRING COMMENT 'Unique article identifier (GUID or link)',
    title STRING COMMENT 'Article headline',
    description STRING COMMENT 'Article summary',
    link STRING COMMENT 'Article URL',
    source_feed STRING COMMENT 'RSS feed URL',
    source_name STRING COMMENT 'Publisher name',
    published_at TIMESTAMP COMMENT 'Publication timestamp',
    ingested_at TIMESTAMP COMMENT 'Pipeline ingestion timestamp',
    language STRING COMMENT 'Detected language (en or si)',
    category STRING COMMENT 'Derived category (disaster, politics, sports, business, general)',
    word_count INT COMMENT 'Total word count of title + description',
    is_breaking BOOLEAN COMMENT 'Whether headline indicates breaking news'
)
USING iceberg
PARTITIONED BY (days(published_at))
TBLPROPERTIES (
    'format-version' = '2',
    'comment' = 'Enriched Sri Lanka news from Lanka Lens RSS feeds via SLNewsEnrichmentJob'
);
