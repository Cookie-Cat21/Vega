select count(*) as record_count
from {{ ref('stg_wiki_events') }}
having count(*) = 0
