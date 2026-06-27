resource "azurerm_virtual_network" "vega" {
  name                = "vnet-vega"
  address_space       = var.vnet_address_space
  location            = azurerm_resource_group.vega.location
  resource_group_name = azurerm_resource_group.vega.name
  tags                = var.tags
}

resource "azurerm_subnet" "aks" {
  name                 = "snet-aks"
  resource_group_name  = azurerm_resource_group.vega.name
  virtual_network_name = azurerm_virtual_network.vega.name
  address_prefixes     = [var.aks_subnet_prefix]
}
