package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.oracle.bmc.identity.model.User
import com.oracle.bmc.identity.requests.ListApiKeysRequest
import com.oracle.bmc.identity.requests.ListAuthTokensRequest
import com.oracle.bmc.identity.requests.ListCustomerSecretKeysRequest
import com.oracle.bmc.identity.requests.ListUsersRequest
import com.oracle.bmc.identity.responses.ListApiKeysResponse
import com.oracle.bmc.identity.responses.ListAuthTokensResponse
import com.oracle.bmc.identity.responses.ListCustomerSecretKeysResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class CustomerSecretKeys : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "All customer secret keys are not older than 90 days in compartment {0}")
    @EnumSource
    fun customerKeysAreNotOlderThan90Days(compartment: Compartments) {
        val expiredSecretKey: MutableList<String> = mutableListOf()

        for (user in listUsers()) {
            val listUserResponse: ListCustomerSecretKeysResponse = identityClient.listCustomerSecretKeys(
                ListCustomerSecretKeysRequest.builder().userId(user.id)
                    .build()
            )
            for (customerSecretKey in listUserResponse.items) {
                val secretKeyCreatedDate: Date = customerSecretKey.timeCreated
                val currentDateMinus90Days: LocalDate = LocalDate.now().minusDays(90)
                val createDateConvertedToLocalDate: LocalDate = secretKeyCreatedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                if (createDateConvertedToLocalDate.isBefore(currentDateMinus90Days)) expiredSecretKey.add(user.id)
            }
        }
        assertEquals(
            0,
            expiredSecretKey.size,
            "Customer Secret keys older than 90 days - " + expiredSecretKey.joinToString()
        )
    }


    @ParameterizedTest(name = "All Auth token are not older than 90 days in compartment {0}")
    @EnumSource
    fun authTokenAreNotOlderThan90Days(compartment: Compartments) {
        val expiredAuthToken: MutableList<String> = mutableListOf()
        for (user in listUsers()) {
            val listAuthTokenResponse: ListAuthTokensResponse = identityClient.listAuthTokens(
                ListAuthTokensRequest.builder().userId(user.id)
                    .build()
            )
            for (authToken in listAuthTokenResponse.items) {
                val authTokenCreatedDate: Date = authToken.timeCreated
                val currentDateMinus90Days: LocalDate = LocalDate.now().minusDays(90)
                val createDateConvertedToLocalDate: LocalDate = authTokenCreatedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                if (createDateConvertedToLocalDate.isBefore(currentDateMinus90Days)) expiredAuthToken.add(user.id)
            }
            assertEquals(
                0,
                expiredAuthToken.size,
                "Customer Auth Token older than 90 days - " + expiredAuthToken.joinToString()
            )
        }

    }

    @ParameterizedTest(name = "All API token are not older than 90 days in compartment {0}")
    @EnumSource
    fun apiTokenAreNotOlderThan90Days(compartment: Compartments) {
        val expiredApiToken: MutableList<String> = mutableListOf()

        for (user in listUsers()) {
            val listApiKeysResponse: ListApiKeysResponse = identityClient.listApiKeys(
                ListApiKeysRequest.builder().userId(user.id)
                    .build()
            )
            for (apiKey in listApiKeysResponse.items) {
                val apiKeyCreatedDate: Date = apiKey.timeCreated
                val currentDateMinus90Days: LocalDate = LocalDate.now().minusDays(90)
                val createDateConvertedToLocalDate: LocalDate = apiKeyCreatedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                if (createDateConvertedToLocalDate.isBefore(currentDateMinus90Days)) expiredApiToken.add(user.id)
            }
            assertEquals(
                0,
                expiredApiToken.size,
                "Customer Api Key older than 90 days - " + expiredApiToken.joinToString()
            )
        }

    }

    private fun listUsers(): MutableList<User> {
        return identityClient
            .listUsers(
                ListUsersRequest.builder()
                    .compartmentId(tenancyConfig.tenancyId).lifecycleState(User.LifecycleState.Active)
                    .build()
            ).items!!
    }
}