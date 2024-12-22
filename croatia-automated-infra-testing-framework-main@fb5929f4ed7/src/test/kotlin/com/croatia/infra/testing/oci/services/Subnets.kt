package com.croatia.infra.testing.oci.services

import com.croatia.infra.testing.oci.BaseOCITest
import com.oracle.bmc.core.model.Subnet
import com.oracle.bmc.core.requests.ListSubnetsRequest

enum class Subnets(
    val subnetName: String,
//                   val virtualCloudNetworks: VirtualCloudNetworks,
    val cidrBlock: String
) {

    OCI_BASTION_SUBNET(
        "oci-bastion-subnet",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.1.0.0/24"
    ),

    GW_LB_SEMI_TRUSTED(
        "gw-lb-semi-trusted",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.1.2.0/24"
    ),

    GW_WORKER_SEMI_TRUSTED(
        "gw-worker-semi-trusted",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.1.1.0/24"
    ),

    APP_WORKER_TRUSTED(
        "app-worker-trusted",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.1.3.0/24"
    ),

    APP_LB_TRUSTED(
        "app-lb-trusted",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.1.4.0/24"
    ),

    NETWORKING_SUBNET(
        "networking-subnet",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.2.0.0/16"
    ),

    SHARED_SERVICES_APP_LB_TRUSTED(
        "app-lb-trusted",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.0.2.0/24"
    ),

    SHARED_SERVICES_APP_WORKER_TRUSTED(
        "app-worker-trusted",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.0.1.0/24"
    ),

    SHARED_SERVICES_OCI_BASTION_SUBNET(
        "oci-bastion-subnet",
//        VirtualCloudNetworks.BOOTSTRAPPING_VCN,
        "10.0.0.0/24"
    ),
}

fun getSubnetsInCompartment(compartment: Compartments): List<Subnet> {
    val compartmentId = getCompartmentByName(compartment.compartmentName)!!.id

    val subnetList = com.croatia.infra.testing.oci.BaseOCITest.virtualNetworkClient.listSubnets(
        ListSubnetsRequest.builder()
            .compartmentId(compartmentId)
            .build()
    )

    return subnetList.items.toList()
}