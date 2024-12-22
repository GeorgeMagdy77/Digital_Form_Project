package com.croatia.infra.testing.oci.environmentTests

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.LocalPeeringGateways
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.core.model.LocalPeeringGateway
import com.oracle.bmc.core.requests.ListLocalPeeringGatewaysRequest
import com.oracle.bmc.identity.model.Compartment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class LocalPeeringGatewayTests : com.croatia.infra.testing.oci.BaseOCITest() {

//    @ParameterizedTest(name = "The {0} VCN should have the expected local peering gateways")
//    @EnumSource
    fun vcnShouldHaveTheExpectedLocalPeeringGateways(lpg: LocalPeeringGateways) {
        val localPeeringGateway: LocalPeeringGateway = getLocalPeeringGateway(lpg)
        assertNotNull(localPeeringGateway, "LPG not present - " + lpg.lpgName)
    }

//    @ParameterizedTest(name = "The {0} LPG should have the expected peer cidr block")
//    @EnumSource
    fun lpgShouldHaveTheExpectedPeerCidrBlock(lpg: LocalPeeringGateways) {
        val localPeeringGateway: LocalPeeringGateway = getLocalPeeringGateway(lpg)
        assertEquals(
            localPeeringGateway.peerAdvertisedCidr,
            lpg.peeredVcn.cidr,
            "LPG Does not have the expected peer advertised cidr block - " + lpg.lpgName
        )
    }

    fun getLocalPeeringGateway(lpg: LocalPeeringGateways): LocalPeeringGateway {
        val compartment: Compartment? = lpg.vcn.compartment.let { getCompartmentByName(it.compartmentName) }
        val listLocalPeeringGateways = virtualNetworkClient.listLocalPeeringGateways(
            ListLocalPeeringGatewaysRequest.builder().compartmentId(compartment?.id).build()
        )
        for (item in listLocalPeeringGateways.items) {
            if (item.lifecycleState.equals(LocalPeeringGateway.LifecycleState.Available) && item.peeringStatus.equals(
                    LocalPeeringGateway.PeeringStatus.Peered
                )
            ) {
                if (item.displayName.equals(lpg.lpgName)) return item
            }
        }
        fail("Local Peering gateway not found - " + lpg.lpgName)
    }
}