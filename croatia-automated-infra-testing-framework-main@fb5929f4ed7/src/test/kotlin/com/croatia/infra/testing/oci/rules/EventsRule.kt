package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.oracle.bmc.events.model.NotificationServiceAction
import com.oracle.bmc.events.model.Rule
import com.oracle.bmc.events.requests.GetRuleRequest
import com.oracle.bmc.events.requests.ListRulesRequest
import com.oracle.bmc.events.responses.GetRuleResponse
import com.oracle.bmc.events.responses.ListRulesResponse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class EventsRule : com.croatia.infra.testing.oci.BaseOCITest() {

    //It's not currently possible to write the tests for this without being able to validate the format of the data being returned
    //I also can't add a rule because we don't have notification topics etc. added

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for IAM group changes in the ROOT compartment")
    fun eventRuleForIAMGroupChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.identitycontrolplane.creategroup") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.deletegroup") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updategroup")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(compliantItems.size > 0, "OCI Event Rule and Notification does not exist for IAM group changes ")
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for IAM policy changes in the ROOT compartment in the ROOT compartment")
    fun eventRuleForIAMPolicyChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.identitycontrolplane.createpolicy") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.deletepolicy") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updatepolicy")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(compliantItems.size > 0, "OCI Event Rule and Notification does not exist for IAM policy changes")
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for Identity Provider Group (IdP) group mapping changes in the ROOT compartment")
    fun eventRuleForIDPGroupMappingChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.identitycontrolplane.createidpgroupmapping") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.deleteidpgroupmapping") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updateidpgroupmapping")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(
            compliantItems.size > 0,
            "OCI Event Rule and Notification does not exist for IAM policy changes in the ROOT compartment"
        )
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for Identity Provider changes")
    fun eventRuleForIDPChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.identitycontrolplane.createidentityprovider") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.deleteidentityprovider") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updateidentityprovider")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(
            compliantItems.size > 0,
            "OCI Event Rule and Notification does not exist for Identity Provider changes in the ROOT compartment"
        )
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for Network Security Groups changes")
    fun eventRuleForNetworkSecurityGroupsChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.virtualnetwork.changenetworksecuritygroupcompartment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.createnetworksecuritygroup") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deletenetworksecuritygroup") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updatenetworksecuritygroup")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(
            compliantItems.size > 0,
            "OCI Event Rule and Notification does not exist for Network Security Groups changes"
        )
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for VCN changes in the ROOT compartment")
    fun eventRuleForVCNChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.virtualnetwork.createvcn") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deletevcn") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updatevcn")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(compliantItems.size > 0, "OCI Event Rule and Notification does not exist for VCN changes")
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for network gateways changes in the ROOT compartment")
    fun eventRuleForNetworkGatewayChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.virtualnetwork.createdrg") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deletedrg") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updatedrg") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.createdrgattachment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deletedrgattachment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updatedrgattachment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.changeinternetgatewaycompartment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.createinternetgateway") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deleteinternetgateway") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updateinternetgateway ") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.changelocalpeeringgatewaycompartment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.createlocalpeeringgateway") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deletelocalpeeringgateway") &&
                    rule.condition.contains("com.oraclecloud.natgateway.changenatgatewaycompartment") &&
                    rule.condition.contains("com.oraclecloud.natgateway.createnatgateway") &&
                    rule.condition.contains("com.oraclecloud.natgateway.deletenatgateway") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.changelocalpeeringgatewaycompartment") &&
                    rule.condition.contains("com.oraclecloud.natgateway.updatenatgateway") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.attachserviceid") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.changeservicegatewaycompartment") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.createservicegateway") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.deleteservicegateway.begin") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.changelocalpeeringgatewaycompartment") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.deleteservicegateway.end") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.detachserviceid") &&
                    rule.condition.contains("com.oraclecloud.servicegateway.updateservicegateway") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updatelocalpeeringgateway")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }
        }
        assertTrue(
            compliantItems.size > 0,
            "OCI Event Rule and Notification does not exist for network gateways changes"
        )
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for route tables changes in the ROOT compartment")
    fun eventRuleForRouteTableChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.virtualnetwork.changeroutetablecompartment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.createroutetable") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updateroutetable") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deleteroutetable")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(compliantItems.size > 0, "OCI Event Rule and Notification does not exist for route tables changes")
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for security list changes in the ROOT compartment")
    fun eventRuleForSecurityListChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.virtualnetwork.changesecuritylistcompartment") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.createsecuritylist") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.deletesecuritylist") &&
                    rule.condition.contains("com.oraclecloud.virtualnetwork.updatesecuritylist")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(compliantItems.size > 0, "OCI Event Rule and Notification does not exist for security list changes")
    }

    @Test
    @DisplayName("OCI Event Rule and Notification should exist for user changes in the ROOT compartment")
    fun eventRuleForUserChangesExists() {
        val compliantItems: MutableList<String> = mutableListOf()
        val listRules: ListRulesResponse = eventsClient.listRules(
            ListRulesRequest.builder().compartmentId(tenancyConfig.tenancyId).lifecycleState(Rule.LifecycleState.Active)
                .build()
        )
        for (rule in listRules.items) {
            if (rule.isEnabled) {
                if (rule.condition.contains("com.oraclecloud.identitycontrolplane.createuser") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.deleteuser") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updateuser") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updateuserstate") &&
                    rule.condition.contains("com.oraclecloud.identitycontrolplane.updateusercapabilities")
                ) {
                    val rule1: GetRuleResponse = eventsClient.getRule(GetRuleRequest.builder().ruleId(rule.id).build())
                    assertNotNull((rule1.rule.actions.actions[0] as NotificationServiceAction).topicId)
                    compliantItems.add(rule.id)
                }
            }

        }
        assertTrue(compliantItems.size > 0, "OCI Event Rule and Notification does not exist for user changes")
    }

}