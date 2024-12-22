data "oci_identity_compartments" "environment" {
  compartment_id = var.tenant_id

  compartment_id_in_subtree = true
  name                      = var.compartment_name
}

data "oci_identity_compartments" "hub" {
  compartment_id = var.tenant_id

  compartment_id_in_subtree = true
  name                      = var.hub_compartment_name
}

data "oci_dns_zones" "sddc" {
  compartment_id = data.oci_identity_compartments.hub.compartments[0].id
  name           = "sddc.jed.oci.oraclecloud.com"
  scope          = "PRIVATE"
}
