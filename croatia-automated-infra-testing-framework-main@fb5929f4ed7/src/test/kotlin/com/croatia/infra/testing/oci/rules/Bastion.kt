package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.bastion.requests.ListBastionsRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class Bastion : com.croatia.infra.testing.oci.BaseOCITest() {

    @Disabled("Bastions should be present?")
    @ParameterizedTest(name = "No bastion should be present in compartment {0}")
    @EnumSource
    fun checkNoBastionsArePresent(compartment: Compartments) {
        val listBastions = bastionClient.listBastions(
            ListBastionsRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id).build()
        )
        Assertions.assertEquals(
            0,
            listBastions.items.size,
            "Bastions are present - " + listBastions.items.joinToString()
        )
    }
}