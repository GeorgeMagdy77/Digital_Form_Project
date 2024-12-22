package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.Region
import com.oracle.bmc.objectstorage.model.BucketSummary
import com.oracle.bmc.objectstorage.requests.*
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse
import com.oracle.bmc.objectstorage.responses.GetReplicationPolicyResponse
import com.oracle.bmc.objectstorage.responses.ListReplicationPoliciesResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ObjectStorage : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "All Object Storage Buckets in compartment {0} should have versioning enabled")
    @EnumSource
    fun objectStorageBucketsHaveVersioningEnabled(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val namespaceName = getNamespace(compartment).value
        for (bucket in listBuckets(compartment, namespaceName)) {
            val bucket1 = objectStorageClient.getBucket(
                GetBucketRequest.builder().bucketName(bucket.name).namespaceName(namespaceName).build()
            )
            if (bucket1.bucket.versioning.value.equals("disabled", true)) offendingItems.add(bucket.name)
        }

        assertEquals(
            0,
            offendingItems.size,
            "Object Storage buckets do not have versioning enabled - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All Object Storage Buckets in compartment {0} should be encrypted with a customer managed key")
    @EnumSource
    fun objectStorageBucketsShouldHaveCMK(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val namespaceName = getNamespace(compartment).value
        for (bucket in listBuckets(compartment, namespaceName)) {
            val bucket1 = objectStorageClient.getBucket(
                GetBucketRequest.builder().bucketName(bucket.name).namespaceName(namespaceName).build()
            )
            if (bucket1.bucket.kmsKeyId == null) offendingItems.add(bucket.name)
        }

        assertEquals(
            0,
            offendingItems.size,
            "Object Storage buckets do not have a customer managed key - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All Object Storage Buckets in compartment {0} should be enabled to emit object events")
    @EnumSource
    fun objectStorageBucketsShouldEmitObjectEvents(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val namespaceName = getNamespace(compartment).value
        for (bucket in listBuckets(compartment, namespaceName)) {
            val bucket1 = objectStorageClient.getBucket(
                GetBucketRequest.builder().bucketName(bucket.name).namespaceName(namespaceName).build()
            )
            if (!bucket1.bucket.objectEventsEnabled) offendingItems.add(bucket.name)
        }

        assertEquals(
            0,
            offendingItems.size,
            "Object Storage buckets are not enabled to emit object events - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All Object Storage Buckets in compartment {0} should not be publicly accessible")
    @EnumSource
    fun objectStorageBucketsShouldNotBePubliclyAccessible(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val namespaceName = getNamespace(compartment).value
        for (bucket in listBuckets(compartment, namespaceName)) {
            val bucket1 = objectStorageClient.getBucket(
                GetBucketRequest.builder().bucketName(bucket.name).namespaceName(namespaceName).build()
            )
            if (!bucket1.bucket.publicAccessType.value.equals("NoPublicAccess", true)) offendingItems.add(bucket.name)
        }
        assertEquals(
            0,
            offendingItems.size,
            "Object Storage buckets are publicly accessible - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All Object Storage Buckets in compartment {0} should not have cross region replication enabled")
    @EnumSource
    fun objectStorageBucketsShouldNotBeReplicatedCrossRegion(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()
        val namespaceName = getNamespace(compartment).value
        for (bucket in listBuckets(compartment, namespaceName)) {
            val listReplicationPolicies: ListReplicationPoliciesResponse = objectStorageClient.listReplicationPolicies(
                ListReplicationPoliciesRequest.builder().bucketName(bucket.name).namespaceName(namespaceName).build()
            )
            for (policy in listReplicationPolicies.items) {
                val replicationPolicy: GetReplicationPolicyResponse = objectStorageClient.getReplicationPolicy(
                    GetReplicationPolicyRequest.builder().bucketName(bucket.name).replicationId(policy.id)
                        .namespaceName(namespaceName).build()
                )
                if (!replicationPolicy.replicationPolicy.destinationRegionName.equals(Region.ME_JEDDAH_1)) offendingItems.add(
                    bucket.name
                )
            }

        }
        assertEquals(
            0,
            offendingItems.size,
            "Object Storage buckets are replicating across regions - " + offendingItems.joinToString()
        )
    }

    private fun getNamespace(compartment: Compartments): GetNamespaceResponse {
        return objectStorageClient.getNamespace(
            GetNamespaceRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id).build()
        )!!
    }

    private fun listBuckets(compartment: Compartments, namespaceName: String): MutableList<BucketSummary> {
        return objectStorageClient.listBuckets(
            ListBucketsRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .namespaceName(namespaceName).build()
        ).items!!
    }

}