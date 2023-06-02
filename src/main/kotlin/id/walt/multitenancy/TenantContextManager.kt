package id.walt.multitenancy

import id.walt.WALTID_DATA_ROOT
import id.walt.multitenancy.FileSystemStoreConfigCreator.makeFileSystemStoreConfig
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import kotlin.io.path.*

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

    /**
     * @return bool, if the context was removed from the context mapping
     */
    fun unloadContext(type: String, id: String): Boolean {
        val tenantId = TenantId.fromString(type, id)
        val tenantIdString = tenantId.toString()

        return when {
            contexts.contains(tenantIdString) -> contexts.remove(tenantIdString) != null
            else -> false
        }
    }

    /**
     * @return bool, if the context was fully deleted
     */
    fun deleteContext(type: String, id: String): Boolean {
        val tenantsDir = Path("$WALTID_DATA_ROOT/data/tenants/")

        val typeDir = tenantsDir.resolve(type)
        if (typeDir.notExists()) throw IllegalArgumentException("This tenant type ($type) is not stored.")

        val tenantDir = typeDir.resolve(id)
        if (tenantDir.notExists()) throw IllegalArgumentException("This tenant ($id) is not stored.")
        unloadContext(type, id)

        return tenantDir.toFile().deleteRecursively()
    }

    fun listAllContextIdsByType(tenantType: TenantType) = Path("$WALTID_DATA_ROOT/data/tenants/${tenantType}/")
        .listDirectoryEntries()
        .map { it.name }

    fun <C : TenantConfig, S : TenantState<C>> getTenantContext(tenantId: TenantId, createState: () -> S): TenantContext<C, S> {
        // TODO: create tenant context according to context configuration
        return contexts[tenantId.toString()] as? TenantContext<C, S> ?: TenantContext(
            tenantId = tenantId,
            hkvStore = FileSystemHKVStore(makeFileSystemStoreConfig("tenants/${tenantId.type}/${tenantId.id}")),
            keyStore = HKVKeyStoreService(),
            vcStore = HKVVcStoreService(),
            state = createState()
        ).also {
            contexts[tenantId.toString()] = it
        }
    }
}
