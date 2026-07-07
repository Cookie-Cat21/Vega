select *
from {{ ref('edit_velocity_by_wiki') }}
where edits_per_minute <= 0
