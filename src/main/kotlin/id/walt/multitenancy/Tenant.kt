package id.walt.multitenancy

import id.walt.services.context.ContextManager
import id.walt.services.hkvstore.HKVKey
import id.walt.webwallet.backend.context.WalletContextManager

abstract class Tenant<C: TenantConfig,S>(private val configFactory: TenantConfigFactory<C>) {
  abstract val tenantType: TenantType
  val CONFIG_KEY = "config"
  val context: TenantContext<S>
    get() = WalletContextManager.currentContext as TenantContext<S>

  var _config: C? = null

  val config: C
    get() = _config ?: if(context.tenantId == TenantId(tenantType, TenantId.DEFAULT_TENANT)) {
      configFactory.forDefaultTenant()
    } else {
      ContextManager.hkvStore.getAsString(HKVKey(CONFIG_KEY))?.let { configFactory.fromJson(it) }
        ?: throw Exception("Tenant config not found")
    }.also {
      _config = it
    }

  val state: S
    get() = context.state

  fun setConfig(config: C) {
    if(context.tenantId == TenantId(tenantType, TenantId.DEFAULT_TENANT)) {
      throw Exception("Can't override default tenant config")
    }
    _config = config
    ContextManager.hkvStore.put(HKVKey(CONFIG_KEY), config.toJson())
  }
}
