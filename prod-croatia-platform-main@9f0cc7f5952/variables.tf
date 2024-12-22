variable "additional_drg_ranges" {
  type        = list(string)
  description = "additional CIDR ranges to be reached from the network via the DRG"
  default     = []
}

variable "cidr_block" {
  type        = string
  description = "IPv4 CIDR block used to deploy the address space for the VNC. Ideally this should be greater than a /18 address space"
}

variable "compartment_name" {
  type        = string
  description = "the name of the compartment to setup the platform in"
}

variable "compute_availability_domain" {
  type        = string
  description = "the oci availability domain to create resources in"
  default     = "pbAP:ME-JEDDAH-1-AD-1"
}

variable "deploy_sddc" {
  type        = bool
  description = "should an sddc be deployed?"
  default     = true
}

variable "environment_name" {
  type        = string
  description = "human readible name for the VCN"
}

variable "extra_subnets" {
  type = map(object({
    cidr_block = string
  }))
  description = "a list of additional subnets to add"
  default     = {}
}

variable "hub_compartment_name" {
  type        = string
  description = "the OCID specifiers for the parent compartment that the VCN will be deployed into"
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

variable "region_id" {
  type        = string
  description = "the OCI region ID"
  default     = "me-jeddah-1"
}

variable "root_domain" {
  type        = string
  description = "the root dns domain for any deployed resources"
}

variable "sddc_vlans" {
  type = map(object({
    cidr_block = string
    private    = bool
  }))
  description = "cidr ranges for the sddc vlans"
  default     = {}
}

variable "tenant_id" {
  type        = string
  description = "The OCID specifiers for the parent compartment that the VCN will be deployed into."
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

variable "ssh_authorized_keys" {
  type        = string
  description = "newline seprated ssh keys authorized to access compute"
  default     = ""
}
