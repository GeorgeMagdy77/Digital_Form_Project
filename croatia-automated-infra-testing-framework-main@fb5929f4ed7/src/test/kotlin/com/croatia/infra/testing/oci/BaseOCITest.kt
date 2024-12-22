package com.croatia.infra.testing.oci

import com.croatia.infra.testing.oci.services.VirtualCloudNetworks
import com.oracle.bmc.ClientConfiguration
import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.Region
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider
import com.oracle.bmc.bastion.BastionClient
import com.oracle.bmc.containerengine.ContainerEngineClient
import com.oracle.bmc.core.BlockstorageClient
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.requests.ListVcnsRequest
import com.oracle.bmc.events.EventsClient
import com.oracle.bmc.filestorage.FileStorageClient
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.Compartment
import com.oracle.bmc.identity.requests.ListCompartmentsRequest
import com.oracle.bmc.identity.responses.ListCompartmentsResponse
import com.oracle.bmc.keymanagement.KmsVaultClient
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.retrier.RetryConfiguration
import com.oracle.bmc.vault.VaultsClient
import com.oracle.bmc.waiter.ExponentialBackoffDelayStrategy
import com.oracle.bmc.waiter.MaxAttemptsTerminationStrategy
import com.sksamuel.hoplite.ConfigLoader
import org.junit.jupiter.api.BeforeAll

data class Tenancy(
    val tenancyId: String,
    val availabilityDomain: String,
    )

open class BaseOCITest {

    companion object {
        val tenancyConfig = ConfigLoader().loadConfigOrThrow<com.croatia.infra.testing.oci.Tenancy>("/tenancy.yml")

        private val retryConfig: RetryConfiguration = RetryConfiguration.builder()
            .delayStrategy(ExponentialBackoffDelayStrategy(10000))
            .terminationStrategy(MaxAttemptsTerminationStrategy(5)).build()

        val provider: BasicAuthenticationDetailsProvider =
            com.croatia.infra.testing.oci.BaseOCITest.Companion.getAuthProvider()

        val identityClient: IdentityClient = IdentityClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )

        val virtualNetworkClient: VirtualNetworkClient = VirtualNetworkClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )

        val blockStorageAccount: BlockstorageClient = BlockstorageClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val eventsClient: EventsClient = EventsClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val computeClient: ComputeClient = ComputeClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val objectStorageClient: ObjectStorageClient = ObjectStorageClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val fileStorageClient: FileStorageClient = FileStorageClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val containerEngineClient: ContainerEngineClient = ContainerEngineClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val vaultsClient: VaultsClient = VaultsClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val kmsVaultsClient: KmsVaultClient = KmsVaultClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )
        val bastionClient: BastionClient = BastionClient(
            com.croatia.infra.testing.oci.BaseOCITest.Companion.provider, ClientConfiguration.builder().retryConfiguration(
                com.croatia.infra.testing.oci.BaseOCITest.Companion.retryConfig
            ).build()
        )

        lateinit var compartmentList: List<Compartment>

        @BeforeAll
        @JvmStatic
        private fun getCompartments() {
            com.croatia.infra.testing.oci.BaseOCITest.Companion.identityClient.setRegion(Region.ME_JEDDAH_1)
            var listCompartmentsResponse: ListCompartmentsResponse = com.croatia.infra.testing.oci.BaseOCITest.Companion.identityClient.listCompartments(
                ListCompartmentsRequest.builder().compartmentId(com.croatia.infra.testing.oci.BaseOCITest.Companion.tenancyConfig.tenancyId).compartmentIdInSubtree(true)
                    .accessLevel(
                        ListCompartmentsRequest.AccessLevel.Any
                    ).lifecycleState(Compartment.LifecycleState.Active).build()
            )
            com.croatia.infra.testing.oci.BaseOCITest.Companion.compartmentList = listCompartmentsResponse.items
        }

        @JvmStatic
        fun getAuthProvider(): BasicAuthenticationDetailsProvider {
            return if (System.getenv("PIPELINE").toBoolean()) {
                InstancePrincipalsAuthenticationDetailsProvider.builder().tenancyId(
                    com.croatia.infra.testing.oci.BaseOCITest.Companion.tenancyConfig.tenancyId
                ).build()
            } else {
                val configFile = ConfigFileReader.parseDefault()
                ConfigFileAuthenticationDetailsProvider(configFile)
            }
        }
    }
}