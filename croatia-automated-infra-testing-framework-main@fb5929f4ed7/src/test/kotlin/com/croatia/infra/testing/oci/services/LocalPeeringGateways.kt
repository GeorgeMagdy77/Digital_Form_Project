package com.croatia.infra.testing.oci.services

enum class LocalPeeringGateways(
    val lpgName: String,
    val vcn: VirtualCloudNetworks,
    val peeredVcn: VirtualCloudNetworks
) {

    EXAMPLE(
        "peering-hub-nprod",
        VirtualCloudNetworks.SHARED_SERVICES_VCN,
        VirtualCloudNetworks.SHARED_SERVICES_VCN
    ),
}