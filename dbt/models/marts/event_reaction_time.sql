select
    natural_event_id,
    natural_event_title,
    category,
    count(*) as correlation_count,
    avg(reaction_time_seconds) as avg_reaction_time_seconds,
    min(reaction_time_seconds) as min_reaction_time_seconds,
    max(reaction_time_seconds) as max_reaction_time_seconds,
    sum(edit_count) as total_correlated_edits,
    max(wiki_article_title) as top_edited_article
from {{ source('vega', 'event_correlations') }}
group by 1, 2, 3
order by avg_reaction_time_seconds asc
