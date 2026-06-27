output "resource_group_name" {
  value = azurerm_resource_group.vega.name
}

output "aks_cluster_name" {
  value = azurerm_kubernetes_cluster.vega.name
}

output "acr_login_server" {
  value = azurerm_container_registry.vega.login_server
}

output "adls_endpoint" {
  value = azurerm_storage_account.adls.primary_dfs_endpoint
}

output "blob_endpoint" {
  value = azurerm_storage_account.blob.primary_blob_endpoint
}
