data "azurerm_client_config" "current" {}

variable "acr_username" {
  description = "ACR username placeholder (set via CI/CD or tfvars; never hardcode)"
  type        = string
  sensitive   = true
  default     = ""
}

variable "acr_password" {
  description = "ACR password placeholder (set via CI/CD or tfvars; never hardcode)"
  type        = string
  sensitive   = true
  default     = ""
}

resource "azurerm_key_vault" "vega" {
  name                       = "kv-vega-${var.environment}"
  location                   = azurerm_resource_group.vega.location
  resource_group_name        = azurerm_resource_group.vega.name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = false
  tags                       = var.tags
}

resource "azurerm_key_vault_access_policy" "deployer" {
  key_vault_id = azurerm_key_vault.vega.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = data.azurerm_client_config.current.object_id

  secret_permissions = [
    "Get",
    "List",
    "Set",
    "Delete",
    "Recover",
    "Purge",
  ]
}

resource "azurerm_key_vault_secret" "acr_login_server" {
  name         = "acr-login-server"
  value        = azurerm_container_registry.vega.login_server
  key_vault_id = azurerm_key_vault.vega.id

  depends_on = [azurerm_key_vault_access_policy.deployer]
}

resource "azurerm_key_vault_secret" "acr_username" {
  name         = "acr-username"
  value        = var.acr_username
  key_vault_id = azurerm_key_vault.vega.id

  depends_on = [azurerm_key_vault_access_policy.deployer]
}

resource "azurerm_key_vault_secret" "acr_password" {
  name         = "acr-password"
  value        = var.acr_password
  key_vault_id = azurerm_key_vault.vega.id

  depends_on = [azurerm_key_vault_access_policy.deployer]
}
