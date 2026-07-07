select *
from {{ ref('top_edited_articles') }}
where title is null
