select count(*) as record_count
from {{ ref('stg_sl_news') }}
having count(*) = 0
