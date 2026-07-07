locals {
  name_prefix = "vega-${var.environment}"

  resource_group_name = var.resource_group_name
  aks_cluster_name    = var.aks_cluster_name

  key_vault_name            = "kv-vega-${var.environment}"
  databricks_workspace_name = "dbw-vega-${var.environment}"

  acr_name          = var.acr_name
  adls_account_name = var.adls_account_name
  blob_account_name = var.blob_account_name

  tags = merge(var.tags, {
    environment = var.environment
  })
}
