with hourly as (
    select
        wiki,
        date_trunc('minute', event_timestamp) as edit_minute,
        count(*) as edits_per_minute
    from {{ ref('stg_wiki_events') }}
    where event_timestamp >= current_timestamp() - interval 1 hour
    group by 1, 2
)
select
    wiki,
    edit_minute,
    edits_per_minute,
    avg(edits_per_minute) over (
        partition by wiki
        order by edit_minute
        rows between 59 preceding and current row
    ) as rolling_hour_avg
from hourly
