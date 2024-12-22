terraform {
  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 4.93"
    }
    nsxt = {
      source  = "vmware/nsxt"
      version = "~> 3.2"
    }
  }
  required_version = "~> 1.2"
}

provider "oci" {
  auth = "InstancePrincipal"

  region = var.region_id
}
