package id.walt.multitenancy

import id.walt.WALTID_DATA_ROOT
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService

object TenantContextManager {
  private val contexts: MutableMap<String, TenantContext<*>> = mutableMapOf()

  // TODO: make context data stores configurable

  fun <S> getTenantContext(tenantId: TenantId, createState: () -> S): TenantContext<S> {
    // TODO: create tenant context according to context configuration
    return contexts.get(tenantId.toString()) as? TenantContext<S> ?: TenantContext(
      tenantId,
      hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/tenants/${tenantId.tenantType}/${tenantId.tenantId}")),
      keyStore = HKVKeyStoreService(),
      vcStore = HKVVcStoreService(),
      state = createState()
    ).also {
      contexts.put(tenantId.toString(), it)
    }
  }
}