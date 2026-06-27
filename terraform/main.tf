terraform {
  required_version = ">= 1.8.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.110.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "vega" {
  name     = var.resource_group_name
  location = var.location
  tags     = var.tags
}
