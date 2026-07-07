select *
from {{ ref('natural_events_by_type') }}
where active_event_count <= 0
