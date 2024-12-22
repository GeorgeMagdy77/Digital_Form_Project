package com.croatia.infra.testing.oci.environmentTests

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.identity.model.Compartment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CompartmentTests : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "The {0} compartment should exist and be active")
    @EnumSource
    fun compartmentShouldExist(compartment: Compartments) {
        assertNotNull(
            getCompartmentByName(compartment.compartmentName),
            "Compartment not found - " + compartment.name
        )
    }

    @ParameterizedTest(name = "The {0} compartment should have the expected parent compartment")
    @EnumSource
    fun compartmentParentTest(compartment: Compartments) {
        val thisCompartment: Compartment? = getCompartmentByName(compartment.compartmentName)
        if (compartment.parent == null) {
            assertTrue(thisCompartment?.compartmentId == tenancyConfig.tenancyId)
        } else {
            assertEquals(thisCompartment?.compartmentId, getCompartmentByName(compartment.parent.compartmentName)?.id)
        }
    }
}