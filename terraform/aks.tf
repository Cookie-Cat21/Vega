resource "azurerm_kubernetes_cluster" "vega" {
  name                = var.aks_cluster_name
  location            = azurerm_resource_group.vega.location
  resource_group_name = azurerm_resource_group.vega.name
  dns_prefix          = "vega-aks"
  kubernetes_version  = "1.30.2"

  default_node_pool {
    name           = "system"
    node_count     = var.aks_node_count
    vm_size        = var.aks_vm_size
    vnet_subnet_id = azurerm_subnet.aks.id
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin = "azure"
    network_policy = "azure"
    service_cidr   = "10.1.0.0/16"
    dns_service_ip = "10.1.0.10"
  }

  role_based_access_control_enabled = true

  tags = var.tags
}

resource "azurerm_kubernetes_cluster_node_pool" "user" {
  name                  = "user"
  kubernetes_cluster_id = azurerm_kubernetes_cluster.vega.id
  vm_size               = var.aks_vm_size
  node_count            = 2
  vnet_subnet_id        = azurerm_subnet.aks.id
  mode                  = "User"
  tags                  = var.tags
}
