# Work factories

Catalog of commit generators used when obvious bugs are exhausted.
See `MILLION_COMMIT_PLAN.md` §6.

| Factory | Unlocked | Example commit |
|---------|----------|----------------|
| `test` | Era 0+ | one edge-case unit test |
| `ops` | Era 0+ | one Prometheus alert rule |
| `config` | Era 0+ | one validated config key |
| `knowledge` | Era 0+ | one ADR or runbook entry |
| `maintenance` | Era 0+ | one dependency bump note |
| `connector_expansion` | Era 2+ | new source connector slice |
| `job_expansion` | Era 1+ | new Flink operator/job slice |
| `analytics_expansion` | Era 3+ | new dbt mart + tests |
| `platform_expansion` | Era 4+ | Helm/GitOps overlay |
| `product_surface` | Era 9+ | API / notification sink slice |

**Saturation rule:** if a factory yields fewer than 5 meaningful batch items, switch factory or advance era unlocks.
