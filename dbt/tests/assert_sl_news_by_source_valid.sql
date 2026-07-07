select *
from {{ ref('sl_news_by_source') }}
where article_count <= 0
