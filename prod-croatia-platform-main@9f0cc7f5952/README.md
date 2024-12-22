## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | ~> 1.2 |
| <a name="requirement_nsxt"></a> [nsxt](#requirement\_nsxt) | ~> 3.2 |
| <a name="requirement_oci"></a> [oci](#requirement\_oci) | ~> 4.93 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_oci"></a> [oci](#provider\_oci) | 4.100.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_openshift_install"></a> [openshift\_install](#module\_openshift\_install) | ./modules/openshift-install | n/a |
| <a name="module_sddc"></a> [sddc](#module\_sddc) | ./modules/sddc | n/a |

## Resources

| Name | Type |
|------|------|
| [oci_core_default_route_table.environment](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/core_default_route_table) | resource |
| [oci_kms_key.master](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/kms_key) | resource |
| [oci_kms_vault.vault](https://registry.terraform.io/providers/oracle/oci/latest/docs/resources/kms_vault) | resource |
| [oci_core_private_ip.edge](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/core_private_ip) | data source |
| [oci_core_route_tables.environment](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/core_route_tables) | data source |
| [oci_core_vcn_dns_resolver_association.environment](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/core_vcn_dns_resolver_association) | data source |
| [oci_core_vcns.local](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/core_vcns) | data source |
| [oci_dns_resolver_endpoints.environment](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/dns_resolver_endpoints) | data source |
| [oci_dns_zones.public](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/dns_zones) | data source |
| [oci_dns_zones.sddc](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/dns_zones) | data source |
| [oci_identity_compartments.environment](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/identity_compartments) | data source |
| [oci_identity_compartments.hub](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/identity_compartments) | data source |
| [oci_identity_compartments.shared](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/identity_compartments) | data source |
| [oci_secrets_secretbundle.nexus_password_bundle](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/secrets_secretbundle) | data source |
| [oci_vault_secrets.nexus_password](https://registry.terraform.io/providers/oracle/oci/latest/docs/data-sources/vault_secrets) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_cluster_ssh_key"></a> [cluster\_ssh\_key](#input\_cluster\_ssh\_key) | the ssh key used to access the ocp cluster(s) | `string` | n/a | yes |
| <a name="input_compartment_name"></a> [compartment\_name](#input\_compartment\_name) | the name of the compartment to setup the platform in | `string` | n/a | yes |
| <a name="input_compute_availability_domain"></a> [compute\_availability\_domain](#input\_compute\_availability\_domain) | the oci availability domain to create resources in | `string` | `"pbAP:ME-JEDDAH-1-AD-1"` | no |
| <a name="input_environment_name"></a> [environment\_name](#input\_environment\_name) | human readible name for the VCN | `string` | n/a | yes |
| <a name="input_hub_compartment_name"></a> [hub\_compartment\_name](#input\_hub\_compartment\_name) | the OCID specifiers for the parent compartment that the VCN will be deployed into | `string` | n/a | yes |
| <a name="input_initial_host_ocpu_count"></a> [initial\_host\_ocpu\_count](#input\_initial\_host\_ocpu\_count) | the initial cpu thread cound for the esxi host for sddc | `number` | `32` | no |
| <a name="input_ocp_clusters"></a> [ocp\_clusters](#input\_ocp\_clusters) | a map of ocp clusters & nsx-t segments to create in the created sddc | <pre>map(object({<br>    cidr_block            = string<br>    segment_cidr_block    = string<br>    dhcp_range            = string<br>    api_vip               = string<br>    ingress_vip           = string<br>    cluster_network       = string<br>    machine_network       = string<br>    service_network       = list(string)<br>    ingress_enabled       = optional(bool)<br>    ingress_lb_ip_address = string<br>  }))</pre> | n/a | yes |
| <a name="input_provisioning_subnet_cidr"></a> [provisioning\_subnet\_cidr](#input\_provisioning\_subnet\_cidr) | CIDR range to be used for the provisioning subnet | `string` | `null` | no |
| <a name="input_region_id"></a> [region\_id](#input\_region\_id) | the OCI region ID | `string` | `"me-jeddah-1"` | no |
| <a name="input_root_domain"></a> [root\_domain](#input\_root\_domain) | the root dns domain for any deployed resources | `string` | n/a | yes |
| <a name="input_sddc_vlans"></a> [sddc\_vlans](#input\_sddc\_vlans) | cidr ranges for the sddc vlans | <pre>map(object({<br>    cidr_block = string<br>    private    = bool<br>  }))</pre> | n/a | yes |
| <a name="input_ssh_authorized_keys"></a> [ssh\_authorized\_keys](#input\_ssh\_authorized\_keys) | newline seprated ssh keys authorized to access compute | `string` | n/a | yes |
| <a name="input_tenant_id"></a> [tenant\_id](#input\_tenant\_id) | The OCID specifiers for the parent compartment that the VCN will be deployed into. | `string` | n/a | yes |

## Outputs

No outputs.
