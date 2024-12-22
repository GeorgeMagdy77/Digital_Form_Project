package com.croatia.infra.testing.oci.services

const val kubernetesVersion: String = "v1.24.1"

enum class Clusters(
    val clusterName: String,
    val compartment: Compartments,
    val vcn: VirtualCloudNetworks,
    val KubernetesVersion: String,
    val privateEndpoint: String,
    val apiEndpointSubnet: Subnets
) {
    SHARED_SERVICES_CLUSTER(
        "sharedservices-cluster",
        Compartments.CVP_SHARED_SERVICES,
        VirtualCloudNetworks.SHARED_SERVICES_VCN,
        kubernetesVersion,
        "10.0.1.244:6443",
        Subnets.APP_WORKER_TRUSTED
    ),

    APP_CLUSTER(
        "app-cluster",
        Compartments.CVP_APPLICATION,
        VirtualCloudNetworks.APPLICATION_VCN,
        kubernetesVersion,
        "10.1.3.196:6443",
        Subnets.APP_WORKER_TRUSTED
    ),

    APIGW_CLUSTER(
        "apigw-cluster",
        Compartments.CVP_APPLICATION,
        VirtualCloudNetworks.APPLICATION_VCN,
        kubernetesVersion,
        "10.1.1.23:6443",
        Subnets.GW_WORKER_SEMI_TRUSTED
    ),
}