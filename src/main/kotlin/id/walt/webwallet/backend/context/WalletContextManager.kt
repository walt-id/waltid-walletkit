package id.walt.webwallet.backend.context

import com.google.common.cache.CacheBuilder
import id.walt.services.context.WaltContext
import id.walt.services.hkvstore.HKVStoreService
import id.walt.services.keystore.KeyStoreService
import id.walt.services.vcstore.VcStoreService
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlin.concurrent.thread

object WalletContextManager : WaltContext() {

  val userContexts = CacheBuilder.newBuilder().maximumSize(256).build(UserContextLoader)
  val threadContexts: HashMap<Long, UserContext> = HashMap()

  val currentContext
    get() = threadContexts.get(Thread.currentThread().id)

  fun setCurrentUserContext(info: UserInfo) {
    threadContexts.put(Thread.currentThread().id, userContexts.get(info.email))
  }

  fun resetCurrentUserContext() {
    threadContexts.remove(Thread.currentThread().id)
  }

  val preRequestHandler
    get() = Handler { ctx -> JWTService.getUserInfo(ctx)?.let { setCurrentUserContext(it) }}

  val postRequestHandler
    get() = Handler { ctx -> resetCurrentUserContext() }

  override fun getHKVStore(): HKVStoreService = currentContext!!.hkvStore

  override fun getKeyStore(): KeyStoreService = currentContext!!.keyStore

  override fun getVcStore(): VcStoreService = currentContext!!.vcStore


}