select
    title,
    wiki,
    count(*) as edit_count
from {{ ref('stg_wiki_events') }}
where event_timestamp >= current_timestamp() - interval 24 hours
group by 1, 2
order by edit_count desc
limit 100
