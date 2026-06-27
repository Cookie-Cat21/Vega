select
    article_id,
    title,
    description,
    link,
    source_feed,
    source_name,
    cast(published_at as timestamp) as published_at,
    cast(ingested_at as timestamp) as ingested_at,
    language,
    category,
    word_count,
    is_breaking
from {{ source('vega', 'sl_news_enriched') }}
