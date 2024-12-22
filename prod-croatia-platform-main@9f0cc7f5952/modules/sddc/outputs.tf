output "nsx_edge_uplink_ip_id" {
  value = oci_ocvp_sddc.sddc.nsx_edge_uplink_ip_id
}

output "nsx_fqdn" {
  value = oci_ocvp_sddc.sddc.nsx_manager_fqdn
}

output "nsx_password" {
  value     = oci_ocvp_sddc.sddc.nsx_manager_initial_password
  sensitive = true
}

output "nsx_username" {
  value = oci_ocvp_sddc.sddc.nsx_manager_username
}

output "vcenter_fqdn" {
  value = oci_ocvp_sddc.sddc.vcenter_fqdn
}

output "vcenter_password" {
  value     = oci_ocvp_sddc.sddc.vcenter_initial_password
  sensitive = true
}

output "vcenter_username" {
  value = oci_ocvp_sddc.sddc.vcenter_username
}
