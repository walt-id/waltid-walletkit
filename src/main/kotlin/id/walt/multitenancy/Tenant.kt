package id.walt.multitenancy

import id.walt.services.context.ContextManager
import id.walt.services.hkvstore.HKVKey
import id.walt.webwallet.backend.context.UserContext
import id.walt.webwallet.backend.context.WalletContextManager
import mu.KotlinLogging
import kotlin.reflect.jvm.jvmName

abstract class Tenant<C : TenantConfig, S : TenantState<C>>(private val configFactory: TenantConfigFactory<C>) {
    class TenantNotFoundException(message: String) : Exception(message)

    private val log = KotlinLogging.logger {  }


    val CONFIG_KEY = "config"


    private fun waltContextStuffErrorsAgain(type: String, extra: String? = null): Nothing = throw WaltContextTenantSystemError(
        "WaltContext system does not work (again)... " +
                "Current context \"${WalletContextManager.currentContext::class.jvmName}\" was casted to TenantContext<C, S>, but is ${
                    when (type) {
                        "otherClass" -> "a different class"
                        "wrongGenericType" -> "a TenantContext, but of different generics types"
                        else -> "UNKNOWN ERROR"
                    }
                }${if (extra == null) "" else " $extra"}"
    )

    data class WaltContextTenantSystemError(override val message: String): Exception()

    val context: TenantContext<C, S>
        get() {
            val currentContext = WalletContextManager.currentContext

            if (currentContext is UserContext) throw IllegalArgumentException("You are authenticated with a user context (authenticated using a user bearer token), but are probably accessing an endpoint meant for tenant contexts (set with {tenantId} in the URL). If you try to use a tenant context method, do not set a user context at the same time, leave the header 'Authentication: Bearer <Token>' from your request.")

            try {
                return currentContext as? TenantContext<C, S> ?: waltContextStuffErrorsAgain("wrongGenericType")
            } catch (e: Exception) {
                log.debug { "Current context: ${currentContext::class.simpleName}: $currentContext" }

                when {
                    e is WaltContextTenantSystemError -> throw e
                    currentContext !is TenantContext<*, *> -> waltContextStuffErrorsAgain("otherClass", e.message)
                    else -> throw WaltContextTenantSystemError("Context/Tenant system error: ${e.message}")
                }
            }
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
