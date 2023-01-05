package id.walt.issuer.backend

import id.walt.multitenancy.Tenant
import id.walt.multitenancy.TenantType

object IssuerTenant : Tenant<IssuerConfig, IssuerState>(IssuerConfig) {
}