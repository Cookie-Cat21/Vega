terraform {
  backend "azurerm" {
    resource_group_name  = "rg-vega-tfstate"
    storage_account_name = "vegatfstate"
    container_name       = "tfstate"
    key                  = "vega.terraform.tfstate"
  }
}
