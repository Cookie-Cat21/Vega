resource "azurerm_container_registry" "vega" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.vega.name
  location            = azurerm_resource_group.vega.location
  sku                 = "Standard"
  admin_enabled       = false
  tags                = var.tags
}

resource "azurerm_role_assignment" "aks_acr_pull" {
  principal_id                     = azurerm_kubernetes_cluster.vega.kubelet_identity[0].object_id
  role_definition_name             = "AcrPull"
  scope                            = azurerm_container_registry.vega.id
  skip_service_principal_aad_check = true
}
