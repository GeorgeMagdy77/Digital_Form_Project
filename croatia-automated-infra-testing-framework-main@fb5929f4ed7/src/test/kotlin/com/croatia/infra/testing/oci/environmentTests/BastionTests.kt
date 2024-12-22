package com.croatia.infra.testing.oci.environmentTests

import com.croatia.infra.testing.oci.services.*
import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.*
import com.oracle.bmc.bastion.model.Bastion
import com.oracle.bmc.bastion.model.BastionSummary
import com.oracle.bmc.bastion.requests.GetBastionRequest
import com.oracle.bmc.bastion.requests.ListBastionsRequest
import com.oracle.bmc.bastion.responses.ListBastionsResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class BastionTests : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "{0} should exist and be in the correct compartment")
    @EnumSource
    fun checkBastionExists(bastion: Bastions) {
        var bastionExists = false
        for (item in getBastionsForCompartments(bastion.compartment)!!.items) {
            if (item.name.equals(bastion.bastionName)) bastionExists = true
        }
        assertTrue(bastionExists)
    }

    @ParameterizedTest(name = "{0} should target the correct VCN")
    @EnumSource
    fun checkVcnTarget(bastion: Bastions) {
        val summary = getBastionSummary(bastion)
        val targetVcn = bastion.targetVcn
        if (targetVcn != null) {
            val vcnId = getVcnByName(targetVcn).id
            assertEquals(vcnId, summary!!.targetVcnId, "Target VCN does not match\n")
        } else {
            // If targetVcn is null, then nothing to check
            assertTrue(true)
        }
    }

    @ParameterizedTest(name = "{0} should target the correct subnet")
    @EnumSource
    fun checkSubnetTarget(bastion: Bastions) {
        val summary = getBastionSummary(bastion)
        val targetSubnet = bastion.targetSubnet
        if (targetSubnet != null) {
            val subnet = getSubnetsInCompartment(bastion.compartment)
                .filter { it.displayName == targetSubnet.subnetName }[0]
            assertEquals(subnet.id, summary!!.targetSubnetId, "Target subnet does not match\n")
        } else {
            // If targetSubnet is null, then nothing to check
            assertTrue(true)
        }
    }

    @Test
    @DisplayName("Only the given bastions should exist")
    fun checkAllBastions() {
        val listOfBastions = mutableListOf<BastionSummary>()
        for (compartment in compartmentList) {
            if (!compartment.lifecycleState.equals("Deleted")) {
                listOfBastions.addAll(
                    bastionClient.listBastions(
                        ListBastionsRequest.builder().compartmentId(compartment.id).build()
                    ).items
                )
            }
        }
        val expected = Bastions.values().size
        val returned = listOfBastions.size
        assertEquals(
            expected,
            returned,
            if (returned < expected) "There are fewer Bastions available than expected\n"
            else "There are extra Bastions than expected\n"
        )
    }

    private fun getBastionsForCompartments(compartment: Compartments): ListBastionsResponse? {
        return bastionClient.listBastions(
            ListBastionsRequest.builder()
                .compartmentId(getCompartmentByName(compartment.compartmentName)?.id).build()
        )
    }

    private fun getBastionSummary(bastion: Bastions): Bastion? {
        val bastionList = getBastionsForCompartments(bastion.compartment)?.items
        val bastionId = bastionList?.filter { it.name == bastion.bastionName }?.get(0)?.id
        return bastionClient.getBastion(GetBastionRequest.builder().bastionId(bastionId).build()).bastion
    }
}