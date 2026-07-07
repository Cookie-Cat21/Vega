terraform {
  # Configure remote state in Azure Storage for team use.
  # Override resource_group_name and storage_account_name for your environment.
  backend "azurerm" {
    resource_group_name  = "rg-vega-tfstate"
    storage_account_name = "vegatfstate"
    container_name       = "tfstate"
    key                  = "vega.terraform.tfstate"
  }
}
