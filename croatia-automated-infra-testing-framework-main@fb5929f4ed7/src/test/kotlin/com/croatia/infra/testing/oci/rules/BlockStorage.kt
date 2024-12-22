package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.core.model.Volume
import com.oracle.bmc.core.requests.GetVolumeBackupPolicyAssetAssignmentRequest
import com.oracle.bmc.core.requests.ListVolumeBackupsRequest
import com.oracle.bmc.core.requests.ListVolumesRequest
import com.oracle.bmc.core.responses.GetVolumeBackupPolicyAssetAssignmentResponse
import com.oracle.bmc.core.responses.ListVolumeBackupsResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class BlockStorage : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "All block volumes in compartment {0} have backup enabled")
    @EnumSource
    fun blockVolumesHaveBackupEnabled(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        for (volume in getAllVolumesInCompartment(compartment)!!) {
            val listVolumesBackup: GetVolumeBackupPolicyAssetAssignmentResponse =
                blockStorageAccount.getVolumeBackupPolicyAssetAssignment(
                    GetVolumeBackupPolicyAssetAssignmentRequest.builder().assetId(volume.id)
                        .build()
                )
            if (listVolumesBackup.items.size == 0) offendingItems.add(volume.id)
        }
        assertEquals(
            0,
            offendingItems.size,
            "Volumes are present without backup enabled - ${offendingItems.joinToString()}"
        )
    }

    @ParameterizedTest(name = "All block volumes in compartment {0} are not encrypted with a customer managed key")
    @EnumSource
    fun blockVolumesAreNotEncryptedWithCustomerManagedKey(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        for (volume in getAllVolumesInCompartment(compartment)!!) {
            if (volume.kmsKeyId == null) offendingItems.add(volume.id)
        }
        assertEquals(
            0,
            offendingItems.size,
            "Volumes are present with a customer managed key - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All block volumes in compartment {0} are restorable")
    @EnumSource
    fun blockVolumesAreRestorable(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val listVolumeBackups: ListVolumeBackupsResponse = blockStorageAccount.listVolumeBackups(
            ListVolumeBackupsRequest.builder()
                .compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .build()
        )
        for (volume in getAllVolumesInCompartment(compartment)!!) {
            var found = false
            for (backup in listVolumeBackups.items) {
                if (volume.id == backup.volumeId) found = true
            }
            if (!found) offendingItems.add(volume.id)
        }
        assertEquals(
            0,
            offendingItems.size,
            "Volumes are present with a customer managed key - " + offendingItems.joinToString()
        )
    }

    private fun getAllVolumesInCompartment(compartment: Compartments): MutableList<Volume>? {
        return blockStorageAccount.listVolumes(
            ListVolumesRequest.builder()
                .compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .build()
        ).items
    }

}