package id.walt.multitenancy

import id.walt.services.context.Context
import id.walt.services.hkvstore.HKVStoreService
import id.walt.services.keystore.KeyStoreService
import id.walt.services.vcstore.VcStoreService

class TenantContext<C : TenantConfig, S : TenantState<C>>(
    val tenantId: TenantId,
    override val hkvStore: HKVStoreService,
    override val keyStore: KeyStoreService,
    override val vcStore: VcStoreService,
    val state: S
) : Context {
    override fun toString() = tenantId.toString()
}
