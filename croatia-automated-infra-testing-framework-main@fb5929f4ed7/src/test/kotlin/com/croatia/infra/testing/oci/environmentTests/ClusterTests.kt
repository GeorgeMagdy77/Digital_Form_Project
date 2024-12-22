package com.croatia.infra.testing.oci.environmentTests

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Clusters
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.croatia.infra.testing.oci.services.getSubnetsInCompartment
import com.oracle.bmc.containerengine.model.ClusterLifecycleState
import com.oracle.bmc.containerengine.model.ClusterSummary
import com.oracle.bmc.containerengine.requests.ListClustersRequest
import com.oracle.bmc.containerengine.responses.ListClustersResponse
import com.oracle.bmc.core.requests.GetVcnRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClusterTests : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "The {0} should be active and exist in the expected compartment")
    @EnumSource
    fun clusterShouldExistInCompartment(cluster: Clusters) {
        var clusterExists = false
        for (item in getClustersForCompartment(cluster.compartment)!!.items) {
            if (item.name.equals(cluster.clusterName)) clusterExists = true
        }
        assertTrue(
            clusterExists,
            "The following cluster does not exist in their expected compartment - " + cluster.clusterName
        )
    }

    @ParameterizedTest(name = "The {0} should have the expected Kubernetes version")
    @EnumSource
    fun clusterShouldHaveTheExpectedKubernetesVersion(cluster: Clusters) {
        var clusterHasCorrectVersion = false
        for (item in getClustersForCompartment(cluster.compartment)!!.items) {
            if (item.kubernetesVersion.equals(cluster.KubernetesVersion)) clusterHasCorrectVersion = true
        }
        assertTrue(
            clusterHasCorrectVersion,
            "The following cluster does not exist in their expected compartment - " + cluster.clusterName
        )
    }

    @ParameterizedTest(name = "The {0} should be in the expected VCN")
    @EnumSource
    fun clusterShouldHaveTheExpectedVCN(cluster: Clusters) {
        var clusterHasCorrectVCN = false
        for (item in getClustersForCompartment(cluster.compartment)!!.items) {
            val vcn = virtualNetworkClient.getVcn(GetVcnRequest.builder().vcnId(item.vcnId).build())
            if (vcn.vcn.displayName.equals(cluster.vcn.vcnName)) clusterHasCorrectVCN = true
        }
        assertTrue(
            clusterHasCorrectVCN,
            "The following cluster does not exist in their expected compartment - " + cluster.clusterName
        )
    }

    @ParameterizedTest(name = "The {0} should use the correct api endpoint subnet")
    @EnumSource
    fun clusterShouldUseTheCorrectApiEndpointSubnet(cluster: Clusters) {
        val clusterInfo =
            getClustersForCompartment(cluster.compartment)!!.items.filter { it.name == cluster.clusterName }[0]
        val subnetId =
            getSubnetsInCompartment(cluster.compartment).filter { it.displayName == cluster.apiEndpointSubnet.subnetName }[0].id
        assertEquals(subnetId, clusterInfo.endpointConfig.subnetId, "Cluster is using the wrong api endpoint subnet\n")
    }

    @ParameterizedTest(name = "The {0} should have the correct endpoints")
    @EnumSource
    fun clusterShouldUseCorrectApiEndpoints(cluster: Clusters) {
        val clusterInfo =
            getClustersForCompartment(cluster.compartment)!!.items.filter { it.name == cluster.clusterName }[0]
        val cidr = cluster.privateEndpoint
        assertEquals(cidr, clusterInfo.endpoints.privateEndpoint)
    }

    @Test
    @DisplayName("OKE clusters should not be assigned a Kubernetes API public endpoint")
    fun clusterShouldNotBeAssignedPublicEndpoint() {
        val offendingItems = mutableListOf<String>()
        for (cluster in listOfClusters) {
            if (cluster.endpointConfig.isPublicIpEnabled) offendingItems.add(cluster.name)
        }
        assertEquals(0, offendingItems.size, "There are clusters with a public endpoint enabled\n")
    }

    @Test
    @DisplayName("Only expected clusters should exist")
    fun onlyExpectedClustersExist() {
        val expected = Clusters.values().size
        val returned = listOfClusters.size
        assertEquals(
            expected,
            returned,
            "The actual and expected number of clusters do not match"
        )
    }

    private val listOfClusters = mutableListOf<ClusterSummary>()

    @BeforeAll
    fun getAllClusters() {
        for (compartment in compartmentList) {
            listOfClusters.addAll(
                containerEngineClient.listClusters(
                    ListClustersRequest.builder().compartmentId(compartment.id)
                        .lifecycleState(ClusterLifecycleState.Active)
                        .build()
                ).items
            )
        }
    }

    private fun getClustersForCompartment(compartment: Compartments): ListClustersResponse? {
        return containerEngineClient.listClusters(
            ListClustersRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .lifecycleState(ClusterLifecycleState.Active)
                .build()
        )
    }
}