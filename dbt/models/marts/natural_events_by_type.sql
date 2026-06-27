select
    category,
    count(*) as active_event_count,
    max(event_timestamp) as latest_event_time,
    avg(latitude) as avg_latitude,
    avg(longitude) as avg_longitude
from {{ ref('stg_natural_events') }}
where not is_closed
group by 1
