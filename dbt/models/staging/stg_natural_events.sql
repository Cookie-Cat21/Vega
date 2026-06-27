select
    event_id,
    title,
    description,
    category,
    source_url,
    latitude,
    longitude,
    cast(event_date as timestamp) as event_timestamp,
    magnitude_value,
    magnitude_unit,
    is_closed,
    cast(ingested_at as timestamp) as ingested_at,
    region_name,
    severity_label
from {{ source('vega', 'natural_events') }}
