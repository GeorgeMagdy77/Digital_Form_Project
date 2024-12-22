package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.core.requests.ListInstancesRequest
import com.oracle.bmc.core.responses.ListInstancesResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class Compute : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "All compute instances in {0} compartment have in transit data encryption enabled")
    @EnumSource
    fun instancesHaveInTransitEncryptionEnabled(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val listInstances: ListInstancesResponse = computeClient.listInstances(
            ListInstancesRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id).build()
        )
        for (instance in listInstances.items) {
            if (!instance.launchOptions.isPvEncryptionInTransitEnabled) offendingItems.add(instance.id)
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "Compute instances have in transit data encryption disabled - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All compute instances in {0} compartment have legacy metadata service endpoint disabled")
    @EnumSource
    fun instancesHaveLegacyMetadataServiceEndpointDisabled(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val listInstances: ListInstancesResponse = computeClient.listInstances(
            ListInstancesRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id).build()
        )
        for (instance in listInstances.items) {
            if (!instance.instanceOptions.areLegacyImdsEndpointsDisabled) offendingItems.add(instance.id)
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "Compute instances have legacy metadata service endpoint enabled - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All compute instances in {0} compartment have monitoring enabled")
    @EnumSource
    fun instancesHaveMonitoringEnabled(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val listInstances: ListInstancesResponse = computeClient.listInstances(
            ListInstancesRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id).build()
        )
        for (instance in listInstances.items) {
            if (instance.agentConfig.isMonitoringDisabled) offendingItems.add(instance.id)
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "Compute instances have legacy metadata service endpoint disabled - " + offendingItems.joinToString()
        )
    }

}