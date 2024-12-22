output "vcn_id" {
  value = oci_core_vcn.spoke.id
}

output "vcn_route_table_id" {
  value = oci_core_route_table.spoke.id
}

output "vcn_cidr_block" {
  value = oci_core_vcn.spoke.cidr_block
}

output "dns_listening_address" {
  value = oci_dns_resolver_endpoint.spoke_dns[0].listening_address
}
