package com.croatia.infra.testing.oci.environmentTests

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Subnets
import com.croatia.infra.testing.oci.services.VirtualCloudNetworks
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.croatia.infra.testing.oci.services.getVcnByName
import com.oracle.bmc.core.model.ServiceGateway
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.ListServiceGatewaysRequest
import com.oracle.bmc.core.requests.ListSubnetsRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class VirtualCloudNetworkTests : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "The {0} should exist in the expected compartment")
    @EnumSource
    fun vcnExistsTest(vcn: VirtualCloudNetworks) {
        val vcnByName: Vcn? = getVcnByName(vcn)
        assertNotNull(vcnByName, "VCN not present - " + vcn.vcnName)
    }

    @ParameterizedTest(name = "The {0} should have the expected CIDR range")
    @EnumSource
    fun vcnShouldHaveTheExpectedCIDR(vcn: VirtualCloudNetworks) {
        val vcnByName: Vcn? = getVcnByName(vcn)
        assertEquals(vcn.cidr, vcnByName!!.cidrBlock, "VCN does not have the expected cidr block")
    }

    @ParameterizedTest(name = "The {0} should contain a service gateway if needed")
    @EnumSource
    fun vcnShouldHaveAServiceGateway(vcn: VirtualCloudNetworks) {
        val vcnByName: Vcn? = getVcnByName(vcn)
        val listServiceGateways = virtualNetworkClient.listServiceGateways(
            ListServiceGatewaysRequest.builder().vcnId(vcnByName?.id)
                .compartmentId(getCompartmentByName(vcn.compartment.compartmentName)?.id).build()
        )
        var sgPresent = false
        for (item in listServiceGateways.items) {
            if (item.lifecycleState.equals(ServiceGateway.LifecycleState.Available)) {
                if (item.services[0].serviceName.equals("All JED Services In Oracle Services Network")) sgPresent =
                    true
            }
        }
        if (!vcn.serviceGatewayPresent) {
            sgPresent = true
        }
        assertTrue(sgPresent, "VCN Does not have a service gateway - " + vcn.vcnName)
    }

    @ParameterizedTest(name = "The {0} should have an available and correct subnets")
    @EnumSource
    fun vcnShouldHaveExpectedSubnets(vcn: VirtualCloudNetworks) {
        val missingSubnets = mutableListOf<Subnets>()
        val listSubnets = virtualNetworkClient.listSubnets(
            ListSubnetsRequest.builder().vcnId(getVcnByName(vcn).id)
                .compartmentId(getCompartmentByName(vcn.compartment.compartmentName)?.id).build()
        ).items.toList().map { it.displayName }
        for (subnet in vcn.subnets) {
            if (!listSubnets.contains(subnet.subnetName)) missingSubnets.add(subnet)
        }
        assertEquals(0, missingSubnets.size, "There are missing subnets:\n $missingSubnets\n")
    }

    @ParameterizedTest(name = "The {0} subnets should have the expected CIDR range")
    @EnumSource
    fun vcnSubnetsShouldHaveExpectedCIDR(vcn: VirtualCloudNetworks) {
        val subnetsWithWrongCidrs = mutableListOf<Subnets>()
        val listSubnets = virtualNetworkClient.listSubnets(
            ListSubnetsRequest.builder().vcnId(getVcnByName(vcn).id)
                .compartmentId(getCompartmentByName(vcn.compartment.compartmentName)?.id).build()
        ).items.toList()
        for (subnet in vcn.subnets) {
            val data = listSubnets.filter { it.displayName == subnet.subnetName }[0]
            if (data.cidrBlock != subnet.cidrBlock) subnetsWithWrongCidrs.add(subnet)
        }
        assertEquals(
            0, subnetsWithWrongCidrs.size,
            "There are subnets present with incorrect CIDR blocks:\n $subnetsWithWrongCidrs\n"
        )
    }
}