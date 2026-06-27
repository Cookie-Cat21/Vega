resource "azurerm_storage_account" "blob" {
  name                     = var.blob_account_name
  resource_group_name      = azurerm_resource_group.vega.name
  location                 = azurerm_resource_group.vega.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  tags                     = var.tags
}

resource "azurerm_storage_container" "flink_checkpoints" {
  name                  = "flink-checkpoints"
  storage_account_name  = azurerm_storage_account.blob.name
  container_access_type = "private"
}

resource "azurerm_storage_container" "tfstate" {
  name                  = "tfstate"
  storage_account_name  = azurerm_storage_account.blob.name
  container_access_type = "private"
}
