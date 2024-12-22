package com.croatia.infra.testing.oci.rules

import com.croatia.infra.testing.oci.BaseOCITest
import com.croatia.infra.testing.oci.services.Compartments
import com.croatia.infra.testing.oci.services.getCompartmentByName
import com.oracle.bmc.core.model.NetworkSecurityGroup
import com.oracle.bmc.core.model.SecurityList
import com.oracle.bmc.core.requests.ListNetworkSecurityGroupSecurityRulesRequest
import com.oracle.bmc.core.requests.ListNetworkSecurityGroupsRequest
import com.oracle.bmc.core.requests.ListSecurityListsRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class VirtualCloudNetwork : com.croatia.infra.testing.oci.BaseOCITest() {

    @ParameterizedTest(name = "All security lists in {0} compartment should not allow all traffic on SSH port (22)")
    @EnumSource
    fun securityListsAllowTrafficOnPort22(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (list in getAllSecurityLists(compartment)) {
            for (ingressRule in list.ingressSecurityRules) {
                if (ingressRule.protocol.equals("all")) offendingItems.add(list.id)
                else if (ingressRule.tcpOptions != null) {
                    if (ingressRule.source.equals("0.0.0.0/0") &&
                        (ingressRule.tcpOptions.destinationPortRange.max == 22 && ingressRule.tcpOptions.destinationPortRange.min == 22 ||
                                ingressRule.tcpOptions.destinationPortRange.max > 22 && ingressRule.tcpOptions.destinationPortRange.min < 22
                                )
                    ) offendingItems.add(list.id)
                } else if (ingressRule.udpOptions != null && ingressRule.protocol != "1") offendingItems.add(list.id)
            }
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "All Security Lists allow all traffic on SSH port (22) - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All security lists in {0} compartment should not allow all traffic on RDP port (3389)")
    @EnumSource
    fun securityListsAllowTrafficOnPort3389(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (list in getAllSecurityLists(compartment)) {
            for (ingressRule in list.ingressSecurityRules) {
                if (ingressRule.protocol.equals("all")) offendingItems.add(list.id)
                else if (ingressRule.tcpOptions != null) {
                    if (ingressRule.source.equals("0.0.0.0/0") &&
                        (ingressRule.tcpOptions.destinationPortRange.max == 3389 && ingressRule.tcpOptions.destinationPortRange.min == 3389 ||
                                ingressRule.tcpOptions.destinationPortRange.max > 3389 && ingressRule.tcpOptions.destinationPortRange.min < 3389
                                )
                    ) offendingItems.add(list.id)
                }
            }
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "All Security Lists allow all traffic on RDP port (3389) - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All security lists in {0} compartment should have stateless ingress rules")
    @EnumSource
    fun securityListsStatefulIngress(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (list in getAllSecurityLists(compartment)) {
            for (ingressRule in list.ingressSecurityRules) {
                if (!ingressRule.isStateless) offendingItems.add(list.id)
            }
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "All Security Lists have stateful ingress rules - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All security lists in {0} compartment should have ingress rules")
    @EnumSource
    fun securityListsHasIngressRules(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (list in getAllSecurityLists(compartment)) {
            if (list.ingressSecurityRules.size == 0) offendingItems.add(list.id)
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "Security Lists have no ingress rules - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All network security groups in {0} compartment should not  all traffic on RDP port (3389)")
    @EnumSource
    fun networkSecurityGroupsAllowAllTrafficOnPort3389(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (nsg in getAllNetworkSecurityGroups(compartment)) {
            val listNetworkSecurityGroupSecurityRules = virtualNetworkClient.listNetworkSecurityGroupSecurityRules(
                ListNetworkSecurityGroupSecurityRulesRequest.builder().networkSecurityGroupId(nsg.id).build()
            )
            for (rule in listNetworkSecurityGroupSecurityRules.items) {
                if (rule.tcpOptions == null && rule.udpOptions == null && !rule.protocol.equals("1")) offendingItems.add(
                    rule.id
                )
                else if (rule.source.equals("0.0.0.0/0") && rule.direction.value.equals("INGRESS", true)) {
                    if ((rule.tcpOptions.destinationPortRange.min == 3389 && rule.tcpOptions.destinationPortRange.max == 3389) ||
                        (rule.tcpOptions.destinationPortRange.min < 3389 && rule.tcpOptions.destinationPortRange.max > 3389) || rule.protocol.equals(
                            "all",
                            true
                        )
                    ) {
                        offendingItems.add(rule.id)
                    }
                }

            }
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "network security groups allow all traffic on RDP port (3389) - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All network security groups in {0} compartment should not  all traffic on SSH port (22)")
    @EnumSource
    fun networkSecurityGroupsAllowAllTrafficOnPort22(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (nsg in getAllNetworkSecurityGroups(compartment)) {
            val listNetworkSecurityGroupSecurityRules = virtualNetworkClient.listNetworkSecurityGroupSecurityRules(
                ListNetworkSecurityGroupSecurityRulesRequest.builder().networkSecurityGroupId(nsg.id).build()
            )
            for (rule in listNetworkSecurityGroupSecurityRules.items) {
                if (rule.tcpOptions == null && rule.udpOptions == null && !rule.protocol.equals("1")) offendingItems.add(
                    rule.id
                )
                else if (rule.source.equals("0.0.0.0/0") && rule.direction.value.equals("INGRESS", true)) {
                    if ((rule.tcpOptions.destinationPortRange.min == 22 && rule.tcpOptions.destinationPortRange.max == 22) ||
                        (rule.tcpOptions.destinationPortRange.min < 22 && rule.tcpOptions.destinationPortRange.max > 22) || rule.protocol.equals(
                            "all",
                            true
                        )
                    ) {
                        offendingItems.add(rule.id)
                    }
                }

            }
        }
        Assertions.assertEquals(
            0,
            offendingItems.size,
            "network security groups allow all traffic on SSH port (22) - " + offendingItems.joinToString()
        )
    }

    @ParameterizedTest(name = "All network security groups in {0} compartment should not have stateful rules")
    @EnumSource
    fun networkSecurityGroupsHaveStatelessRules(compartment: Compartments) {
        val offendingItems: MutableList<String> = mutableListOf()

        for (nsg in getAllNetworkSecurityGroups(compartment)) {
            val listNetworkSecurityGroupSecurityRules = virtualNetworkClient.listNetworkSecurityGroupSecurityRules(
                ListNetworkSecurityGroupSecurityRulesRequest.builder().networkSecurityGroupId(nsg.id).build()
            )
            for (rule in listNetworkSecurityGroupSecurityRules.items) {
                if (rule.direction.value.equals("INGRESS", true) && !rule.isStateless) offendingItems.add(rule.id)
            }
        }

        Assertions.assertEquals(
            0,
            offendingItems.size,
            "network security groups have stateful ingress rules configured - " + offendingItems.joinToString()
        )
    }

    private fun getAllSecurityLists(compartment: Compartments): MutableList<SecurityList> {
        return virtualNetworkClient.listSecurityLists(
            ListSecurityListsRequest.builder().compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .build()
        ).items!!
    }

    private fun getAllNetworkSecurityGroups(compartment: Compartments): MutableList<NetworkSecurityGroup> {
        return virtualNetworkClient.listNetworkSecurityGroups(
            ListNetworkSecurityGroupsRequest.builder()
                .compartmentId(getCompartmentByName(compartment.compartmentName)?.id)
                .lifecycleState(NetworkSecurityGroup.LifecycleState.Available).build()
        ).items!!
    }
}