data "oci_core_private_ip" "vcenter" {
  private_ip_id = oci_ocvp_sddc.sddc.vcenter_private_ip_id
}

data "oci_core_private_ip" "nsx" {
  private_ip_id = oci_ocvp_sddc.sddc.nsx_manager_private_ip_id
}
