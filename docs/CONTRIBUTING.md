# Contributing to Vega

Thank you for contributing to Vega. This guide covers local setup, testing, commit conventions, and the pull request process.

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
./scripts/register-connectors.sh
./scripts/submit-jobs.sh
make monitoring
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
| `k8s/` | Kubernetes manifests |
| `terraform/` | Azure infrastructure |
| `scripts/` | Operational helper scripts |
| `docs/` | Architecture and contributor documentation |

## Running Tests

Run all module tests:

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

Before opening a pull request, run the full validation suite:

```bash
make validate
```

This runs `docker compose config` on both compose files and `terraform validate`.

Additional checks:

```bash
cd dbt && dbt compile
```

## Coding Standards

1. **Java 21** — Use records, virtual threads, pattern matching, and sealed interfaces where appropriate.
2. **Flink DataStream API only** — Never use the Table API.
3. **Avro schemas** — Define contracts in `.avsc` files; generate Java classes via `avro-maven-plugin`.
4. **No hardcoded secrets** — Use environment variables; update `.env.example` for new vars.
5. **Tests required** — Every Java class gets a corresponding `*Test.java`. Integration tests use `*IT.java` naming.
6. **Minimal comments** — Prefer clear naming over Javadoc boilerplate.

## Commit Conventions

Use descriptive commit messages in this format:

```
Phase N: short description
```

For improvement work outside phased delivery:

```
Improve: category — specific change
```

Examples:

- `Phase 4: Flink stream processing jobs — 5 jobs`
- `Improve: test coverage — add edge case tests for all operators`

One logical change per commit. Do not mix unrelated changes.

## Pull Request Process

1. Fork the repository and create a feature branch.
2. Make your changes with tests.
3. Run `make test` and `make validate`.
4. Fill out the pull request template (`.github/PULL_REQUEST_TEMPLATE.md`).
5. Request review. CI must pass before merge.

## Dependency Updates

Dependabot opens weekly Maven PRs for all connector modules and `flink-jobs`. Review and merge dependency updates promptly to stay current with security patches.

## Reporting Issues

Include:

- Steps to reproduce
- Expected vs actual behavior
- Relevant logs from Flink UI, Kafka Connect, or connector stdout
- Environment (local Docker Compose vs AKS)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
