package id.walt.multitenancy

import id.walt.WALTID_DATA_ROOT
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService

object TenantContextManager {
  private val contexts: MutableMap<String, TenantContext<*, *>> = mutableMapOf()

  // TODO: make context data stores configurable

  fun <C: TenantConfig, S: TenantState<C>> getTenantContext(tenantId: TenantId, createState: () -> S): TenantContext<C, S> {
    // TODO: create tenant context according to context configuration
    return contexts.get(tenantId.toString()) as? TenantContext<C, S> ?: TenantContext(
      tenantId,
      hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/tenants/${tenantId.type}/${tenantId.id}")),
      keyStore = HKVKeyStoreService(),
      vcStore = HKVVcStoreService(),
      state = createState()
    ).also {
      contexts.put(tenantId.toString(), it)
    }
  }
}