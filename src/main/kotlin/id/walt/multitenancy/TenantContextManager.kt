package id.walt.multitenancy

import id.walt.WALTID_DATA_ROOT
import id.walt.issuer.backend.IssuerManager
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.verifier.backend.VerifierManager
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object TenantContextManager {
    private val contexts: MutableMap<String, TenantContext<*, *>> = mutableMapOf()

    // TODO: make context data stores configurable

    fun listLoadedContexts(): List<TenantId> {
        return listOf(
            //IssuerManager.getIssuerContext(TenantId.DEFAULT_TENANT).tenantId, // Preload issuer context
            //VerifierManager.getService().getVerifierContext(TenantId.DEFAULT_TENANT).tenantId, // Preload verifier context
            *contexts.values.map { it.tenantId }.toTypedArray(),
        ).distinct()
    }

    fun listAllContextIdsByType(tenantType: TenantType) = Path("$WALTID_DATA_ROOT/data/tenants/${tenantType}/")
        .listDirectoryEntries()
        .map { it.name }

    fun <C : TenantConfig, S : TenantState<C>> getTenantContext(tenantId: TenantId, createState: () -> S): TenantContext<C, S> {
        // TODO: create tenant context according to context configuration
        return contexts[tenantId.toString()] as? TenantContext<C, S> ?: TenantContext(
            tenantId = tenantId,
            hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/tenants/${tenantId.type}/${tenantId.id}")),
            keyStore = HKVKeyStoreService(),
            vcStore = HKVVcStoreService(),
            state = createState()
        ).also {
            contexts[tenantId.toString()] = it
        }
    }
}
