module "spoke" {
  source = "./modules/oci-spoke"

  compartment_id        = data.oci_identity_compartments.environment.compartments[0].id
  environment_name      = var.environment_name
  extra_subnets         = var.extra_subnets
  freeform_tags         = local.freeform_tags
  cidr_block            = var.cidr_block
  hub_compartment_id    = data.oci_identity_compartments.hub.compartments[0].id
  spoke_cidr_block      = var.spoke_cidr_block
  spoke_dns_label       = var.spoke_dns_label
  spoke_dns_subnet      = var.spoke_dns_subnet
  spoke_vcn_cidr_block  = var.spoke_vcn_cidr_block
  additional_drg_ranges = var.additional_drg_ranges
}

module "sddc" {
  source = "./modules/sddc"

  count = var.deploy_sddc ? 1 : 0

  compartment_id              = data.oci_identity_compartments.environment.compartments[0].id
  compute_availability_domain = var.compute_availability_domain
  dns_zone_id                 = data.oci_dns_zones.sddc.zones[0].id
  environment_name                = var.environment_name
  freeform_tags               = local.freeform_tags
  hub_compartment_id          = data.oci_identity_compartments.hub.compartments[0].id
  initial_host_ocpu_count     = var.initial_host_ocpu_count
  provisioning_subnet_cidr    = var.provisioning_subnet_cidr
  sddc_vlans                  = var.sddc_vlans
  ssh_authorized_keys         = var.ssh_authorized_keys
  vcn_id                      = module.spoke.vcn_id
  virtual_network_cidr_block  = module.spoke.vcn_cidr_block
}

resource "oci_core_default_route_table" "environment" {
  count = var.deploy_sddc ? 1 : 0

  manage_default_resource_id = module.spoke.vcn_route_table_id

  route_rules {
    network_entity_id = module.sddc[0].nsx_edge_uplink_ip_id
    destination       = var.spoke_cidr_block
    destination_type  = "CIDR_BLOCK"
  }
}
