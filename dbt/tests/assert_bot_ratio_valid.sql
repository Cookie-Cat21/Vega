select *
from {{ ref('bot_vs_human_ratio') }}
where bot_percentage < 0
   or bot_percentage > 100
