variable "additional_drg_ranges" {
  type        = list(string)
  description = "additional CIDR ranges to be reached from the network via the DRG"
  default     = []
}

variable "cidr_block" {
  type        = string
  description = "IPv4 CIDR block used to deploy the address space for the VNC. Ideally this should be greater than a /18 address space"
}

variable "compartment_id" {
  type        = string
  description = "id of the sddc oci compartment"
}

variable "environment_name" {
  type        = string
  description = "human readible name for the VCN"
}

variable "extra_subnets" {
  type        = map(string)
  description = "a list of additional subnets to add"
  default     = {}
}

variable "freeform_tags" {
  type        = map(string)
  description = "oci freeform resource tags"
  default     = {}
}

variable "hub_compartment_id" {
  type        = string
  description = "OCID of the hub compartment"
}

variable "hub_name" {
  type        = string
  description = "display name of the hub vcn"
  default     = "hub"
}

variable "spoke_cidr_block" {
  type        = string
  description = "CIDR block for the spoke network as a whole"
}

variable "spoke_dns_label" {
  type        = string
  description = "label for the dns shortname of the spoke network"
  default     = null
}

variable "spoke_dns_subnet" {
  type        = string
  description = "CIDR block for the dns subnet"
}

variable "spoke_vcn_cidr_block" {
  type        = string
  description = "CIDR block for VCN in OCI"
}
