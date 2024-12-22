package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.oracle.bmc.identity.model.Group
import com.oracle.bmc.identity.model.User
import com.oracle.bmc.identity.requests.*
import com.oracle.bmc.identity.responses.GetAuthenticationPolicyResponse
import com.oracle.bmc.identity.responses.ListUsersResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class IAM : com.croatia.infra.testing.oci.BaseOCITest() {

    @Test
    @DisplayName("All IAM users have a verified email address")
    fun iamUsersHaveValidAndVerifiedEmailAddresses() {
        val offendingItems: MutableList<String> = mutableListOf()
        val listUsers: ListUsersResponse =
            identityClient.listUsers(
                ListUsersRequest.builder().externalIdentifier(null).compartmentId(tenancyConfig.tenancyId)
                    .lifecycleState(User.LifecycleState.Active).build()
            )
        for (user in listUsers.items) {
            if (user.email == null || !user.emailVerified) offendingItems.add(user.id)
        }
        assertEquals(
            0,
            offendingItems.size,
            "IAM users have no or unvalidated e mail address - " + offendingItems.joinToString()
        )
    }

    @Test
    @DisplayName("All IAM users have MFA Enabled")
    fun iamUsersHaveMFAEnabled() {
        val offendingItems: MutableList<String> = mutableListOf()
        val listUsers: ListUsersResponse =
            identityClient.listUsers(
                ListUsersRequest.builder().externalIdentifier(null).compartmentId(tenancyConfig.tenancyId)
                    .lifecycleState(User.LifecycleState.Active).build()
            )
        for (user in listUsers.items) {
            if (!user.isMfaActivated) offendingItems.add(user.id)
        }
        assertEquals(
            0,
            offendingItems.size,
            "IAM users do not have MFA enabled - " + offendingItems.joinToString()
        )
    }

    @Test
    @DisplayName("IAM PasswordPolicy should require lowercase character")
    fun iamPoliciesRequireLowerCaseChar() {
        val authenticationPolicy: GetAuthenticationPolicyResponse = identityClient.getAuthenticationPolicy(
            GetAuthenticationPolicyRequest.builder().compartmentId(tenancyConfig.tenancyId).build()
        )
        assertTrue(
            authenticationPolicy.authenticationPolicy.passwordPolicy.isLowercaseCharactersRequired,
            "Lowercase Chars are not required in IAM Authentication policy"
        )
    }

    @Test
    @DisplayName("IAM PasswordPolicy should require uppercase character")
    fun iamPoliciesRequireUpperCaseChar() {
        val authenticationPolicy: GetAuthenticationPolicyResponse = identityClient.getAuthenticationPolicy(
            GetAuthenticationPolicyRequest.builder().compartmentId(tenancyConfig.tenancyId).build()
        )
        assertTrue(
            authenticationPolicy.authenticationPolicy.passwordPolicy.isUppercaseCharactersRequired,
            "Uppercase Chars are not required in IAM Authentication policy"
        )
    }

    @Test
    @DisplayName("IAM PasswordPolicy should require special character")
    fun iamPoliciesRequireSpecialChar() {
        val authenticationPolicy: GetAuthenticationPolicyResponse = identityClient.getAuthenticationPolicy(
            GetAuthenticationPolicyRequest.builder().compartmentId(tenancyConfig.tenancyId).build()
        )
        assertTrue(
            authenticationPolicy.authenticationPolicy.passwordPolicy.isSpecialCharactersRequired,
            "Special Chars are not required in IAM Authentication policy"
        )
    }

    @Test
    @DisplayName("IAM PasswordPolicy should require numeric character")
    fun iamPoliciesRequireNumericChar() {
        val authenticationPolicy: GetAuthenticationPolicyResponse = identityClient.getAuthenticationPolicy(
            GetAuthenticationPolicyRequest.builder().compartmentId(tenancyConfig.tenancyId).build()
        )
        assertTrue(
            authenticationPolicy.authenticationPolicy.passwordPolicy.isNumericCharactersRequired,
            "Numeric Chars are not required in IAM Authentication policy"
        )
    }

    @Test
    @DisplayName("IAM PasswordPolicy should require minimum of 14 characters")
    fun iamPoliciesRequireMinimumOf14Characters() {
        val authenticationPolicy: GetAuthenticationPolicyResponse = identityClient.getAuthenticationPolicy(
            GetAuthenticationPolicyRequest.builder().compartmentId(tenancyConfig.tenancyId).build()
        )
        assertTrue(
            authenticationPolicy.authenticationPolicy.passwordPolicy.minimumPasswordLength >= 14,
            "Minimum password length is not at least 14 characters - " + authenticationPolicy.authenticationPolicy.passwordPolicy.minimumPasswordLength
        )
    }

    @Test
    @DisplayName("Administrator users should not have Api Keys associated")
    fun adminUsersHaveApiKeysAssociated() {
        val offendingItems: MutableList<String> = mutableListOf()
        val listGroups = identityClient.listGroups(
            ListGroupsRequest.builder().compartmentId(tenancyConfig.tenancyId)
                .lifecycleState(Group.LifecycleState.Active).build()
        )

        for (group in listGroups.items) {
            if (group.name.equals("Administrators", true)) {
                val listUserGroupMemberships = identityClient.listUserGroupMemberships(
                    ListUserGroupMembershipsRequest.builder().compartmentId(tenancyConfig.tenancyId)
                        .groupId(group.id).build()
                )
                for (membership in listUserGroupMemberships.items) {
                    val userApiKeys =
                        identityClient.listApiKeys(ListApiKeysRequest.builder().userId(membership.userId).build())
                    if (userApiKeys.items.size > 0) offendingItems.add(membership.userId)

                }
            }
        }
        assertEquals(0, offendingItems.size, "Administrator users have api keys- " + offendingItems.joinToString())
    }
}