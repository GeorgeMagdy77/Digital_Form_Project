package com.croatia.infra.testing.oci.services

import com.croatia.infra.testing.oci.BaseOCITest
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.ListVcnsRequest

enum class VirtualCloudNetworks(
    val vcnName: String,
    val compartment: Compartments,
    val cidr: String,
    val subnets: List<Subnets>,
    val serviceGatewayPresent: Boolean
) {

    APPLICATION_VCN(
        "application_vcn",
        Compartments.CVP_APPLICATION,
        "10.1.0.0/16",
        listOf(
            Subnets.OCI_BASTION_SUBNET,
            Subnets.GW_LB_SEMI_TRUSTED,
            Subnets.GW_WORKER_SEMI_TRUSTED,
            Subnets.APP_WORKER_TRUSTED,
            Subnets.APP_LB_TRUSTED
        ),
        true
    ),

    SHARED_SERVICES_VCN(
        "sharedservices-vcn",
        Compartments.CVP_SHARED_SERVICES,
        "10.0.0.0/16",
        listOf(
            Subnets.SHARED_SERVICES_APP_LB_TRUSTED,
            Subnets.SHARED_SERVICES_APP_WORKER_TRUSTED,
            Subnets.SHARED_SERVICES_OCI_BASTION_SUBNET
        ),
        true
    ),
}

fun getVcnByName(vcn: VirtualCloudNetworks): Vcn {
    val listOfVcns = com.croatia.infra.testing.oci.BaseOCITest.virtualNetworkClient.listVcns(
        ListVcnsRequest.builder().compartmentId(getCompartmentByName(vcn.compartment.compartmentName)?.id).build()
    ).items
    return listOfVcns.filter { it.displayName == vcn.vcnName && it.lifecycleState == Vcn.LifecycleState.Available }[0]
}