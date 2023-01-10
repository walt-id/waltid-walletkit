package id.walt.verifier.backend

import id.walt.multitenancy.Tenant
import id.walt.multitenancy.TenantType

object VerifierTenant : Tenant<VerifierConfig, VerifierState>(VerifierConfig) {
}