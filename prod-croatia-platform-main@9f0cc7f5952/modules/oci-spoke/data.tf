data "oci_core_drgs" "hub" {
  compartment_id = var.hub_compartment_id
}

data "oci_core_vcns" "hub" {
  compartment_id = var.hub_compartment_id
  display_name   = var.hub_name
}

data "oci_core_subnets" "vpn" {
  compartment_id = var.hub_compartment_id
  display_name   = "${var.hub_name}-vpn-subnet"
}

data "oci_core_services" "all_oci_services" {
  filter {
    name   = "name"
    values = ["All .* Services In Oracle Services Network"]
    regex  = true
  }
}

data "oci_core_vcn_dns_resolver_association" "spoke" {
  count = var.spoke_dns_subnet != null ? 1 : 0

  vcn_id = oci_core_vcn.spoke.id
}
