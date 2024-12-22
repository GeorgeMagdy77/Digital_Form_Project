## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | ~> 1.2 |
| <a name="requirement_oci"></a> [oci](#requirement\_oci) | ~> 4.93 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_oci"></a> [oci](#provider\_oci) | ~> 4.93 |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [oci_core_network_security_group.private](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_network_security_group) | resource |
| [oci_core_network_security_group.public](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_network_security_group) | resource |
| [oci_core_network_security_group_security_rule.private](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_network_security_group_security_rule) | resource |
| [oci_core_network_security_group_security_rule.private_ingress](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_network_security_group_security_rule) | resource |
| [oci_core_network_security_group_security_rule.public_egress](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_network_security_group_security_rule) | resource |
| [oci_core_network_security_group_security_rule.public_ingress](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_network_security_group_security_rule) | resource |
| [oci_core_security_list.provisioning](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_security_list) | resource |
| [oci_core_subnet.provisioning](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_subnet) | resource |
| [oci_core_vlan.sddc_vlans](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_vlan) | resource |
| [oci_dns_rrset.nsx](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/dns_rrset) | resource |
| [oci_dns_rrset.vcenter](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/dns_rrset) | resource |
| [oci_ocvp_sddc.sddc](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/ocvp_sddc) | resource |
| [oci_core_private_ip.nsx](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/core_private_ip) | data source |
| [oci_core_private_ip.vcenter](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/core_private_ip) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_compartment_id"></a> [compartment\_id](#input\_compartment\_id) | id of the sddc oci compartment | `string` | n/a | yes |
| <a name="input_compute_availability_domain"></a> [compute\_availability\_domain](#input\_compute\_availability\_domain) | the oci availability domain to create resources in | `string` | `"pbAP:ME-JEDDAH-1-AD-1"` | no |
| <a name="input_dns_zone_id"></a> [dns\_zone\_id](#input\_dns\_zone\_id) | ocid of the public dns zone | `string` | n/a | yes |
| <a name="input_environment_name"></a> [environment\_name](#input\_environment\_name) | human readible name for the VCN | `string` | n/a | yes |
| <a name="input_freeform_tags"></a> [freeform\_tags](#input\_freeform\_tags) | oci freeform resource tags | `map(string)` | `{}` | no |
| <a name="input_hub_compartment_id"></a> [hub\_compartment\_id](#input\_hub\_compartment\_id) | id of the sddc oci network hub compartment | `string` | n/a | yes |
| <a name="input_initial_host_ocpu_count"></a> [initial\_host\_ocpu\_count](#input\_initial\_host\_ocpu\_count) | the initial cpu thread cound for the esxi host for sddc | `number` | `32` | no |
| <a name="input_provisioning_subnet_cidr"></a> [provisioning\_subnet\_cidr](#input\_provisioning\_subnet\_cidr) | CIDR range to be used for the provisioning subnet | `string` | `null` | no |
| <a name="input_sddc_vlans"></a> [sddc\_vlans](#input\_sddc\_vlans) | cidr ranges for the sddc vlans | <pre>map(object({<br>    cidr_block = string<br>    private    = bool<br>  }))</pre> | n/a | yes |
| <a name="input_ssh_authorized_keys"></a> [ssh\_authorized\_keys](#input\_ssh\_authorized\_keys) | newline seprated ssh keys authorized to access compute | `string` | n/a | yes |
| <a name="input_vcn_id"></a> [vcn\_id](#input\_vcn\_id) | ocid of the vcn to deploy into | `string` | n/a | yes |
| <a name="input_virtual_network_cidr_block"></a> [virtual\_network\_cidr\_block](#input\_virtual\_network\_cidr\_block) | cidr block of the virtual network | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_nsx_edge_uplink_ip_id"></a> [nsx\_edge\_uplink\_ip\_id](#output\_nsx\_edge\_uplink\_ip\_id) | n/a |
| <a name="output_nsx_fqdn"></a> [nsx\_fqdn](#output\_nsx\_fqdn) | n/a |
| <a name="output_nsx_password"></a> [nsx\_password](#output\_nsx\_password) | n/a |
| <a name="output_nsx_username"></a> [nsx\_username](#output\_nsx\_username) | n/a |
| <a name="output_vcenter_fqdn"></a> [vcenter\_fqdn](#output\_vcenter\_fqdn) | n/a |
| <a name="output_vcenter_password"></a> [vcenter\_password](#output\_vcenter\_password) | n/a |
| <a name="output_vcenter_username"></a> [vcenter\_username](#output\_vcenter\_username) | n/a |
