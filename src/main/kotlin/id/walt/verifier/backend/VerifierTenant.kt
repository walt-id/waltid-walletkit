package id.walt.verifier.backend

import id.walt.multitenancy.Tenant

object VerifierTenant : Tenant<VerifierConfig, VerifierState>(VerifierConfig)
