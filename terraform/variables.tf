variable "resource_group_name" {
  description = "Azure resource group name for Vega infrastructure"
  type        = string
  default     = "rg-vega-prod"
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "eastus"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  default     = "prod"
}

variable "aks_cluster_name" {
  description = "AKS cluster name"
  type        = string
  default     = "vega-aks"
}

variable "aks_node_count" {
  description = "Default node pool size"
  type        = number
  default     = 3
}

variable "aks_vm_size" {
  description = "AKS node VM size"
  type        = string
  default     = "Standard_D4s_v5"
}

variable "acr_name" {
  description = "Azure Container Registry name (globally unique)"
  type        = string
}

variable "adls_account_name" {
  description = "ADLS Gen2 storage account name for Iceberg"
  type        = string
}

variable "blob_account_name" {
  description = "Blob storage account name for Flink checkpoints"
  type        = string
}

variable "vnet_address_space" {
  description = "VNet address space"
  type        = list(string)
  default     = ["10.0.0.0/16"]
}

variable "aks_subnet_prefix" {
  description = "AKS subnet prefix"
  type        = string
  default     = "10.0.1.0/24"
}

variable "tags" {
  description = "Resource tags"
  type        = map(string)
  default = {
    project     = "vega"
    managed_by  = "terraform"
    environment = "prod"
  }
}
