package com.croatia.infra.testing.oci.services

import com.croatia.infra.testing.oci.BaseOCITest
import com.oracle.bmc.identity.model.Compartment

enum class Compartments(val compartmentName: String, val parent: Compartments?) {

    // Parent Compartment
    // Compartments with parents set to null are those who have a parent as the root compartment
    // Potential fix -> Implement getCompartment in setup as listCompartment returns child compartments
//    ALPHA_OCI("alphaoci", null),

    // Child Compartments
    CROATIA("croatia", null),

    CVP_APPLICATION("cvp-application", null),
    JEDDAH("Jeddah", CVP_APPLICATION),
    NON_PROD("non-prod", JEDDAH),
    DATA_PLATFORM("Data_Platform", NON_PROD),

    DIGITAL_BANKING_PLATFORM("Digital_Banking_Platform", NON_PROD),
    DEV("Dev", DIGITAL_BANKING_PLATFORM),
    PRE_PROD("Pre_Prod", DIGITAL_BANKING_PLATFORM),
    UAT("Uat", DIGITAL_BANKING_PLATFORM),

    HUB("Hub", NON_PROD),
    SHARED_NETWORK("Shared_Network", NON_PROD),
    SHARED_SECURITY("Shared_Security", NON_PROD),

    PROD("non-prod", JEDDAH),
    DATA_PLATFORM_PROD("Data_Platform", PROD),

    DIGITAL_BANKING_PLATFORM_PROD("Digital_Banking_Platform", PROD),
    DBP_PROD("Uat", DIGITAL_BANKING_PLATFORM_PROD),

    HUB_PROD("Hub", PROD),
    SHARED_NETWORK_PROD("Shared_Network", PROD),
    SHARED_SECURITY_PROD("Shared_Security", PROD),


    CVP_NETWORK("cvp-network", null),
    CVP_SHARED_SERVICES("cvp-sharedservices", null),
    MANAGED_COMPARTMENT_FOR_PAAS("ManagedCompartmentForPaaS", null),
    TEST_COMPARTMENT("test-compartment", null),
}

fun getCompartmentByName(compartment: String): Compartment? {
    for (c in com.croatia.infra.testing.oci.BaseOCITest.compartmentList) {
        if (c.name == compartment) return c
    }
    return null
}