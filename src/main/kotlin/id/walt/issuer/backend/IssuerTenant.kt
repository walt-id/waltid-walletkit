package id.walt.issuer.backend

import id.walt.multitenancy.Tenant

object IssuerTenant : Tenant<IssuerConfig, IssuerState>(IssuerConfig)
