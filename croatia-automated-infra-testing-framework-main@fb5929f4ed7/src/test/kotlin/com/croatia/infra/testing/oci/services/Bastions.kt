package com.croatia.infra.testing.oci.services

enum class Bastions(
    val bastionName: String,
    val compartment: Compartments,
    val targetVcn: VirtualCloudNetworks?,
    val targetSubnet: Subnets?
) {
    APPLICATION_BASTION(
        "application-bastion",
        Compartments.CVP_APPLICATION,
        VirtualCloudNetworks.APPLICATION_VCN,
        Subnets.OCI_BASTION_SUBNET
    ),

    SHARED_SERVICES_BASTION(
        "sharedservices-bastion",
        Compartments.CVP_SHARED_SERVICES,
        VirtualCloudNetworks.SHARED_SERVICES_VCN,
        Subnets.OCI_BASTION_SUBNET
    ),
}