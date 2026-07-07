select count(*) as record_count
from {{ ref('stg_natural_events') }}
having count(*) = 0
