variable "acr_name" {
  description = "Azure Container Registry name (globally unique)"
  type        = string

  validation {
    condition     = length(var.acr_name) >= 5 && length(var.acr_name) <= 50
    error_message = "acr_name must be between 5 and 50 characters."
  }

  validation {
    condition     = can(regex("^[a-zA-Z0-9]+$", var.acr_name))
    error_message = "acr_name must contain only alphanumeric characters."
  }
}

variable "adls_account_name" {
  description = "ADLS Gen2 storage account name for Iceberg"
  type        = string

  validation {
    condition     = length(var.adls_account_name) >= 3 && length(var.adls_account_name) <= 24
    error_message = "adls_account_name must be between 3 and 24 characters."
  }

  validation {
    condition     = can(regex("^[a-z0-9]+$", var.adls_account_name))
    error_message = "adls_account_name must contain only lowercase letters and numbers."
  }
}
