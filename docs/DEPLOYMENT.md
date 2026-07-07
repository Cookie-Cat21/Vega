# Deployment Guide

## Local

```bash
./scripts/bootstrap-local.sh
```

## Production (AKS)

1. `terraform apply` in `terraform/`
2. Configure GitHub secrets (ACR, Azure credentials)
3. Push to `main` — CI builds images and deploy workflow patches tags

See `docs/RUNBOOK.md` for operations.
