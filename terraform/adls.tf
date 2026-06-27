resource "azurerm_storage_account" "adls" {
  name                     = var.adls_account_name
  resource_group_name      = azurerm_resource_group.vega.name
  location                 = azurerm_resource_group.vega.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
  is_hns_enabled           = true
  tags                     = var.tags
}

resource "azurerm_storage_data_lake_gen2_filesystem" "iceberg" {
  name               = "iceberg"
  storage_account_id = azurerm_storage_account.adls.id
}
