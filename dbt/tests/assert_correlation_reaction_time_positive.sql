select *
from {{ source('vega', 'event_correlations') }}
where reaction_time_seconds <= 0
