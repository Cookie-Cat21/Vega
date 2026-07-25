# Deployment Guide

## Local

```bash
cp .env.example .env
make bootstrap   # live connectors + all Flink jobs
# or
make demo        # bounded fixtures → file sink (no live APIs)
```

See `docs/LOCAL_DEMO.md`.

## Azure / AKS (scaffolding)

Requires Azure credentials and GitHub secrets. Without them, Terraform apply and `deploy-to-aks.yml` will fail — that is expected for a portfolio clone.

1. Configure `.env` / GitHub secrets (ACR, Azure credentials)
2. `terraform apply` in `terraform/` (only with a real subscription)
3. Push to `main` — CI builds images; deploy workflow patches tags when secrets exist

See `docs/RUNBOOK.md` for operations.
