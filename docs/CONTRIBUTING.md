# Contributing to Vega

Thank you for contributing to Vega. This guide covers local setup, testing, and the pull request process.

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9+
- Docker Compose v2
- Terraform 1.8+ (for infrastructure changes)
- Python 3.12+ and dbt 1.8+ (for analytics changes)

## Getting Started

```bash
git clone https://github.com/Cookie-Cat21/Vega.git
cd Vega
cp .env.example .env

# Full local bootstrap (build, start stack, register connectors, submit jobs)
make bootstrap

# Or step by step:
make build
make up
make register-connectors
make submit-jobs
make monitoring
```

Bounded local proof (fixture events → Flink file sink):

```bash
make demo
```

Tear down when finished:

```bash
make teardown
```

## Project Layout

| Directory | Purpose |
|---|---|
| `connectors/` | Kafka Connect source connectors (Maven modules) |
| `flink-jobs/` | Flink stream processing jobs |
| `iceberg/schemas/` | Iceberg table DDL |
| `dbt/` | Databricks analytics models |
| `k8s/` | Kubernetes manifests (Azure AKS scaffolding) |
| `terraform/` | Azure infrastructure scaffolding |
| `scripts/` | Operational helper scripts |
| `docs/` | Architecture and contributor documentation |

## Running Tests

```bash
make test
```

Run tests for a single module:

```bash
cd connectors/wikimedia && mvn test
cd connectors/eonet && mvn test
cd connectors/slnews && mvn test
cd flink-jobs && mvn test
```

Integration tests (`*IT.java`) are disabled by default and require a running Kafka stack. Enable them locally after `make up` and `./scripts/wait-for-kafka.sh`.

## Validation

Before opening a pull request, run:

```bash
make validate
make test
```

`make validate` checks Compose config and `terraform validate` (no cloud apply).

Optional analytics check (needs placeholder Databricks env vars — see `.env.example`):

```bash
cd dbt && dbt compile
```

## Coding Standards

1. **Java 21** — Prefer records, pattern matching, and clear naming.
2. **Flink DataStream API** — Jobs in this repo use the DataStream API.
3. **Avro schemas** — Define contracts in `.avsc` files; generate Java via `avro-maven-plugin` where used.
4. **No hardcoded secrets** — Use environment variables; update `.env.example` for new vars.
5. **Tests required** — New Java logic should ship with `*Test.java`. Integration stubs use `*IT.java`.
6. **Minimal comments** — Prefer clear naming over Javadoc boilerplate.

## Commit Conventions

Use descriptive commit messages, for example:

```
Remove unused DLQ wiring from docs
Fix Flink Compose Kafka bootstrap for local demo
```

One logical change per commit. Do not mix unrelated changes.

## Pull Request Process

1. Fork the repository and create a feature branch.
2. Make your changes with tests.
3. Run `make test` and `make validate`.
4. Fill out the pull request template (`.github/PULL_REQUEST_TEMPLATE.md`).
5. Request review. CI should pass before merge (unit tests do not require Azure secrets).

## Dependency Updates

Dependabot opens weekly Maven PRs for connector modules and `flink-jobs`. Review and merge promptly for security patches.

## Reporting Issues

Include:

- Steps to reproduce
- Expected vs actual behavior
- Relevant logs from Flink UI, Kafka Connect, or connector stdout
- Environment (local Docker Compose vs AKS)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
