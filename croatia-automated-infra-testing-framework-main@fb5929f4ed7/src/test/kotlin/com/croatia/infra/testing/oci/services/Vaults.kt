package com.croatia.infra.testing.oci.services

enum class Vaults(val vaultName: String, val compartment: Compartments?) {

    APPLICATION_VAULT("application-vault", Compartments.CVP_APPLICATION),

    SHARED_SERVICES_VAULT("sharedservices-vault", Compartments.CVP_SHARED_SERVICES),

    ALPHA_VAULT("alpha-vault", Compartments.CVP_SHARED_SERVICES),
}