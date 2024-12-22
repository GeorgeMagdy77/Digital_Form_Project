package com.croatia.infra.testing.oci.environmentTests

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.Vaults
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.keymanagement.model.VaultSummary
import com.oracle.bmc.keymanagement.requests.ListVaultsRequest
import com.oracle.bmc.keymanagement.responses.ListVaultsResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class VaultTests : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "The {0} should exist and be active in the expected compartment")
    @EnumSource
    fun vaultsShouldExistInCompartment(vault: Vaults) {
        val parentCompartment: Compartments? = vault.compartment
        val listVaults: ListVaultsResponse = if (parentCompartment == null) kmsVaultsClient.listVaults(
            ListVaultsRequest.builder().compartmentId(
                tenancyConfig.tenancyId
            ).build()
        )
        else kmsVaultsClient.listVaults(
            ListVaultsRequest.builder().compartmentId(getCompartmentByName(vault.compartment.compartmentName)?.id)
                .build()
        )
        var vaultExists = false
        for (item in listVaults.items) {
            if (item.lifecycleState.equals(VaultSummary.LifecycleState.Active)) {
                if (item.displayName.equals(vault.vaultName)) vaultExists = true
            }
        }
        assertTrue(vaultExists, "The following vault does not exist in the expected compartment - " + vault.name)
    }
}