select
    source_name,
    category,
    language,
    count(*) as article_count,
    sum(case when is_breaking then 1 else 0 end) as breaking_count,
    max(published_at) as latest_article_at
from {{ ref('stg_sl_news') }}
where published_at >= current_timestamp() - interval 24 hours
group by 1, 2, 3
order by article_count desc
