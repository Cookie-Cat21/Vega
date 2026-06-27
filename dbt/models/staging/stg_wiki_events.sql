select
    id,
    title,
    user,
    bot,
    wiki,
    server_url,
    cast(timestamp as timestamp) as event_timestamp,
    type,
    namespace,
    comment,
    length_old,
    length_new,
    revision_old,
    revision_new,
    edit_size_delta,
    language_group,
    is_new_article
from {{ source('vega', 'wiki_events_enriched') }}
