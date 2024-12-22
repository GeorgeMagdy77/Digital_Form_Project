variable "compartment_id" {
  type        = string
  description = "id of the sddc oci compartment"
}

variable "compute_availability_domain" {
  type        = string
  description = "the oci availability domain to create resources in"
  default     = "pbAP:ME-JEDDAH-1-AD-1"
}

variable "dns_zone_id" {
  type        = string
  description = "ocid of the public dns zone"
}

variable "environment_name" {
  type        = string
  description = "human readible name for the VCN"
}

variable "freeform_tags" {
  type        = map(string)
  description = "oci freeform resource tags"
  default     = {}
}

variable "hub_compartment_id" {
  type        = string
  description = "id of the sddc oci network hub compartment"
}

variable "initial_host_ocpu_count" {
  type        = number
  description = "the initial cpu thread cound for the esxi host for sddc"
  default     = 32
}

variable "provisioning_subnet_cidr" {
  type        = string
  description = "CIDR range to be used for the provisioning subnet"
  default     = null
}

variable "sddc_vlans" {
  type = map(object({
    cidr_block = string
    private    = bool
  }))
  description = "cidr ranges for the sddc vlans"
}

variable "ssh_authorized_keys" {
  type        = string
  description = "newline seprated ssh keys authorized to access compute"
}

variable "vcn_id" {
  type        = string
  description = "ocid of the vcn to deploy into"
}

variable "virtual_network_cidr_block" {
  type        = string
  description = "cidr block of the virtual network"
}
