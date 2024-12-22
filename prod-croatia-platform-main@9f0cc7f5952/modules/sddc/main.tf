resource "oci_core_security_list" "provisioning" {
  display_name   = "${var.environment_name}-provisioning"
  compartment_id = var.compartment_id
  vcn_id         = var.vcn_id

  egress_security_rules {
    description = "allow all egress traffic"
    destination = "0.0.0.0/0"
    protocol    = "all"
  }

  ingress_security_rules {
    description = "allow all ingress traffic"
    source      = "0.0.0.0/0"
    protocol    = "all"
  }
}

resource "oci_core_subnet" "provisioning" {
  cidr_block        = var.provisioning_subnet_cidr
  display_name      = "${var.environment_name}-provisioning"
  compartment_id    = var.compartment_id
  vcn_id            = var.vcn_id
  security_list_ids = [oci_core_security_list.provisioning.id]

  prohibit_public_ip_on_vnic = true

  freeform_tags = var.freeform_tags

  lifecycle {
    ignore_changes = [
      route_table_id,
      defined_tags
    ]
  }
}

resource "oci_core_network_security_group" "public" {
  display_name   = "${var.environment_name}-public"
  compartment_id = var.compartment_id
  vcn_id         = var.vcn_id

  freeform_tags = var.freeform_tags
}

resource "oci_core_network_security_group_security_rule" "public_ingress" {
  network_security_group_id = oci_core_network_security_group.public.id
  direction                 = "INGRESS"
  protocol                  = "all"
  source                    = "0.0.0.0/0"
  source_type               = "CIDR_BLOCK"
}

resource "oci_core_network_security_group_security_rule" "public_egress" {
  network_security_group_id = oci_core_network_security_group.public.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
}

resource "oci_core_network_security_group" "private" {
  display_name   = "${var.environment_name}-private"
  compartment_id = var.compartment_id
  vcn_id         = var.vcn_id

  freeform_tags = var.freeform_tags
}

resource "oci_core_network_security_group_security_rule" "private_ingress" {
  network_security_group_id = oci_core_network_security_group.private.id
  direction                 = "INGRESS"
  protocol                  = "all"
  source                    = var.virtual_network_cidr_block
  source_type               = "CIDR_BLOCK"
}

resource "oci_core_network_security_group_security_rule" "private" {
  network_security_group_id = oci_core_network_security_group.private.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
}

resource "oci_core_vlan" "sddc_vlans" {
  for_each = var.sddc_vlans

  display_name   = "${var.environment_name}-${each.key}"
  cidr_block     = each.value.cidr_block
  compartment_id = var.compartment_id
  vcn_id         = var.vcn_id
  nsg_ids        = [each.value.private ? oci_core_network_security_group.private.id : oci_core_network_security_group.public.id]

  freeform_tags = var.freeform_tags
}

resource "oci_ocvp_sddc" "sddc" {
  compartment_id              = var.compartment_id
  compute_availability_domain = var.compute_availability_domain
  esxi_hosts_count            = 1
  provisioning_subnet_id      = oci_core_subnet.provisioning.id
  ssh_authorized_keys         = var.ssh_authorized_keys
  vmware_software_version     = "7.0 update 3"

  nsx_edge_uplink1vlan_id = oci_core_vlan.sddc_vlans["nsx_edge_uplink1vlan_id"].id
  nsx_edge_uplink2vlan_id = oci_core_vlan.sddc_vlans["nsx_edge_uplink2vlan_id"].id
  nsx_edge_vtep_vlan_id   = oci_core_vlan.sddc_vlans["nsx_edge_vtep_vlan_id"].id
  nsx_vtep_vlan_id        = oci_core_vlan.sddc_vlans["nsx_vtep_vlan_id"].id
  vmotion_vlan_id         = oci_core_vlan.sddc_vlans["vmotion_vlan_id"].id
  vsan_vlan_id            = oci_core_vlan.sddc_vlans["vsan_vlan_id"].id
  vsphere_vlan_id         = oci_core_vlan.sddc_vlans["vsphere_vlan_id"].id
  replication_vlan_id     = oci_core_vlan.sddc_vlans["replication"].id
  provisioning_vlan_id    = oci_core_vlan.sddc_vlans["provisioning"].id

  display_name            = var.environment_name
  is_single_host_sddc     = true
  initial_host_ocpu_count = var.initial_host_ocpu_count
  initial_host_shape_name = "BM.DenseIO.E4.128"
  initial_sku             = "HOUR"
  is_hcx_enabled          = false

  freeform_tags = var.freeform_tags
}

resource "oci_dns_rrset" "vcenter" {
  domain          = "vcenter-${var.environment_name}.sddc.jed.oci.oraclecloud.com"
  rtype           = "A"
  zone_name_or_id = var.dns_zone_id
  compartment_id  = var.hub_compartment_id

  items {
    domain = "vcenter-${var.environment_name}.sddc.jed.oci.oraclecloud.com"
    rdata  = data.oci_core_private_ip.vcenter.ip_address
    rtype  = "A"
    ttl    = "900"
  }
}

resource "oci_dns_rrset" "nsx" {
  domain          = "nsx-${var.environment_name}.sddc.jed.oci.oraclecloud.com"
  rtype           = "A"
  zone_name_or_id = var.dns_zone_id
  compartment_id  = var.hub_compartment_id

  items {
    domain = "nsx-${var.environment_name}.sddc.jed.oci.oraclecloud.com"
    rdata  = data.oci_core_private_ip.nsx.ip_address
    rtype  = "A"
    ttl    = "900"
  }
}
