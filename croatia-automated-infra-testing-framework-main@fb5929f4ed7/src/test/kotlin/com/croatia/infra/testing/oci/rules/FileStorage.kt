package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.filestorage.model.FileSystemSummary
import com.oracle.bmc.filestorage.requests.GetExportRequest
import com.oracle.bmc.filestorage.requests.ListExportsRequest
import com.oracle.bmc.filestorage.requests.ListFileSystemsRequest
import com.oracle.bmc.filestorage.responses.ListExportsResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class FileStorage : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "All File Systems in compartment {0} should not be publicly accessible")
    @EnumSource
    fun fileSystemsAreNotPubliclyAccessible(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (fileSystem in listAllActiveFileSystems(compartment)!!) {
            val listExports: ListExportsResponse = fileStorageClient.listExports(
                ListExportsRequest.builder()
                    .compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                    .fileSystemId(fileSystem.id).build()
            )

            for (export in listExports.items) {
                val export1 = fileStorageClient.getExport(GetExportRequest.builder().exportId(export.id).build())
                for (option in export1.export.exportOptions) {
                    if (option.source.equals("0.0.0.0/0") && !option.requirePrivilegedSourcePort &&
                        option.access.value.equals("READ_WRITE") &&
                        option.identitySquash.value.equals("NONE")
                    ) offendingItems.add(fileSystem.id)
                }
            }
        }

        assertEquals(0, offendingItems.size, "File Systems are Publicly Accessible- " + offendingItems.joinToString())
    }

    @ParameterizedTest(name = "All File Systems in compartment {0} should not be restricted to root users")
    @EnumSource
    fun fileSystemsAreNotRestrictedToRootUsers(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (fileSystem in listAllActiveFileSystems(compartment)!!) {
            val listExports: ListExportsResponse = fileStorageClient.listExports(
                ListExportsRequest.builder()
                    .compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                    .fileSystemId(fileSystem.id).build()
            )

            for (export in listExports.items) {
                val export1 = fileStorageClient.getExport(GetExportRequest.builder().exportId(export.id).build())
                for (option in export1.export.exportOptions) {
                    if (option.identitySquash.value.equals("ROOT") &&
                        option.anonymousGid != 65534L &&
                        option.anonymousUid != 65534L
                    ) offendingItems.add(fileSystem.id)
                }
            }
        }

        assertEquals(
            0,
            offendingItems.size,
            "File Systems are restricted to root users - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All File Systems in compartment {0} should not be encrypted with a Customer Managed Key (CMK)")
    @EnumSource
    fun fileSystemsAreNotEncryptedWithACustomerManagedKey(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (fileSystem in listAllActiveFileSystems(compartment)!!) {
            if (fileSystem.kmsKeyId.isEmpty()) offendingItems.add(fileSystem.id)
        }

        assertEquals(
            0,
            offendingItems.size,
            "encrypted with a Customer Managed Key (CMK) - " + offendingItems.joinToString()
        )
    }

    private fun listAllActiveFileSystems(compartment: Compartments): MutableList<FileSystemSummary>? {
        return fileStorageClient.listFileSystems(
            ListFileSystemsRequest.builder()
                .compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .availabilityDomain(tenancyConfig.availabilityDomain)
                .lifecycleState(ListFileSystemsRequest.LifecycleState.Active).build()
        ).items
    }

}