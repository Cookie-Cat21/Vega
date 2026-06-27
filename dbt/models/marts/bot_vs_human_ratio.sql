select
    wiki,
    date_trunc('hour', event_timestamp) as edit_hour,
    count(*) as total_edits,
    sum(case when bot then 1 else 0 end) as bot_edits,
    sum(case when not bot then 1 else 0 end) as human_edits,
    round(100.0 * sum(case when bot then 1 else 0 end) / count(*), 2) as bot_percentage
from {{ ref('stg_wiki_events') }}
group by 1, 2
