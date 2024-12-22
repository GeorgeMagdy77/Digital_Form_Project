resource "oci_core_vcn" "spoke" {
  display_name   = var.environment_name
  cidr_block     = var.spoke_vcn_cidr_block
  compartment_id = var.compartment_id
  dns_label      = var.spoke_dns_label

  freeform_tags = var.freeform_tags
}

resource "oci_core_route_table" "spoke" {

  compartment_id = var.compartment_id
  vcn_id         = oci_core_vcn.spoke.id
  display_name   = "${var.environment_name}-private"

  freeform_tags = var.freeform_tags

  lifecycle {
    ignore_changes = [
      route_rules
    ]
  }
}

resource "oci_core_drg_attachment" "spoke_private" {
  display_name = "${var.environment_name}-private"
  drg_id       = data.oci_core_drgs.hub.drgs[0].id

  network_details {
    id             = oci_core_vcn.spoke.id
    route_table_id = oci_core_route_table.spoke.id
    type           = "VCN"
    vcn_route_type = "VCN_CIDRS"
  }

  freeform_tags = var.freeform_tags
}

resource "oci_core_nat_gateway" "spoke" {
  vcn_id         = oci_core_vcn.spoke.id
  display_name   = "${var.environment_name}-nat"
  compartment_id = var.compartment_id

  freeform_tags = var.freeform_tags
}

resource "oci_core_service_gateway" "spoke" {
  display_name   = "${var.environment_name}-svc-gw"
  vcn_id         = oci_core_vcn.spoke.id
  compartment_id = var.compartment_id

  services {
    service_id = data.oci_core_services.all_oci_services.services[0].id
  }

  freeform_tags = var.freeform_tags
}

resource "oci_core_default_route_table" "spoke" {
  manage_default_resource_id = oci_core_vcn.spoke.default_route_table_id

  route_rules {
    network_entity_id = oci_core_service_gateway.spoke.id
    description       = "Default route to force service based traffic to the service gateway."
    destination       = lookup(data.oci_core_services.all_oci_services.services[0], "cidr_block")
    destination_type  = "SERVICE_CIDR_BLOCK"
  }

  route_rules {
    network_entity_id = oci_core_nat_gateway.spoke.id
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
  }

  route_rules {
    network_entity_id = data.oci_core_drgs.hub.drgs[0].id
    destination       = var.cidr_block
    destination_type  = "CIDR_BLOCK"
  }

  dynamic "route_rules" {
    for_each = var.additional_drg_ranges
    content {
      network_entity_id = data.oci_core_drgs.hub.drgs[0].id
      destination       = route_rules.value
      destination_type  = "CIDR_BLOCK"
    }
  }

  freeform_tags = var.freeform_tags
}

resource "oci_core_security_list" "spoke_dns" {
  count = var.spoke_dns_subnet != null ? 1 : 0

  display_name   = "${var.environment_name}-dns"
  compartment_id = var.compartment_id
  vcn_id         = oci_core_vcn.spoke.id

  ingress_security_rules {
    source   = "0.0.0.0/0"
    protocol = "17"

    udp_options {
      max = 53
      min = 53
    }
  }

  freeform_tags = var.freeform_tags
}

resource "oci_core_subnet" "spoke_dns" {
  count = var.spoke_dns_subnet != null ? 1 : 0

  cidr_block        = var.spoke_dns_subnet
  display_name      = "${var.environment_name}-dns"
  compartment_id    = var.compartment_id
  vcn_id            = oci_core_vcn.spoke.id
  security_list_ids = [oci_core_security_list.spoke_dns[0].id]

  prohibit_public_ip_on_vnic = true

  freeform_tags = var.freeform_tags

  lifecycle {
    ignore_changes = [
      route_table_id,
      defined_tags
    ]
  }
}

resource "oci_core_security_list" "extra_subnets" {
  for_each = var.sddc_vlans

  display_name   = "${var.environment_name}-${each.key}"
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

resource "oci_core_subnet" "extra_subnets" {
  for_each = var.sddc_vlans

  cidr_block        = each.value
  display_name      = "${var.environment_name}-${each.key}"
  compartment_id    = var.compartment_id
  vcn_id            = oci_core_vcn.spoke.id
  security_list_ids = [oci_core_security_list.extra_subnets[each.key].id]

  prohibit_public_ip_on_vnic = true

  freeform_tags = var.freeform_tags

  lifecycle {
    ignore_changes = [
      route_table_id,
      defined_tags
    ]
  }
}

resource "oci_dns_resolver_endpoint" "spoke_dns" {
  count = var.spoke_dns_subnet != null ? 1 : 0

  is_forwarding = false
  is_listening  = true
  name          = replace(var.environment_name, "-", "")
  resolver_id   = data.oci_core_vcn_dns_resolver_association.spoke[0].dns_resolver_id
  subnet_id     = oci_core_subnet.spoke_dns[0].id
  scope         = "PRIVATE"
}
