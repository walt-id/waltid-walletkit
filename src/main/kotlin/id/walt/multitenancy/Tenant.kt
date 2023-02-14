package id.walt.multitenancy

import id.walt.services.context.ContextManager
import id.walt.services.hkvstore.HKVKey
import id.walt.webwallet.backend.context.WalletContextManager
import kotlin.reflect.jvm.jvmName

abstract class Tenant<C : TenantConfig, S : TenantState<C>>(private val configFactory: TenantConfigFactory<C>) {
    class TenantNotFoundException(message: String) : Exception(message)

    val CONFIG_KEY = "config"


    private fun waltContextStuffErrorsAgain(type: String): Nothing = throw IllegalStateException(
        "WaltContext system does not work (again)... " +
                "Current context \"${WalletContextManager.currentContext::class.jvmName}\" was casted to TenantContext<C, S>, but is ${
                    when (type) {
                        "otherClass" -> "a different class"
                        "wrongGenericType" -> "a TenantContext, but of different generics types"
                        else -> "UNKNOWN ERROR"
                    }
                }"
    )

    val context: TenantContext<C, S>
        get() {
            val currentContext = WalletContextManager.currentContext

            if (currentContext is TenantContext<*, *>) return currentContext as? TenantContext<C, S>
                ?: waltContextStuffErrorsAgain("wrongGenericType")
            else waltContextStuffErrorsAgain("otherClass")
        }

    val tenantId: TenantId
        get() = context.tenantId

    val config: C
        get() = context.state.config ?: ContextManager.hkvStore.getAsString(HKVKey(CONFIG_KEY))
            ?.let { configFactory.fromJson(it) } ?: if (context.tenantId.id == TenantId.DEFAULT_TENANT) {
            configFactory.forDefaultTenant()
        } else {
            throw TenantNotFoundException("Tenant config not found")
        }.also {
            context.state.config = it
        }

    val state: S
        get() = context.state

    fun setConfig(config: C) {
        context.state.config = config
        ContextManager.hkvStore.put(HKVKey(CONFIG_KEY), config.toJson())
    }
}
