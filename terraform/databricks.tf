resource "azurerm_databricks_workspace" "vega" {
  name                = "dbw-vega-${var.environment}"
  resource_group_name = azurerm_resource_group.vega.name
  location            = azurerm_resource_group.vega.location
  sku                 = "standard"
  tags                = var.tags
}
