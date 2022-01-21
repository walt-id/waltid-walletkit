package id.walt.webwallet.backend.context

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import id.walt.services.context.Context
import id.walt.services.context.WaltIdContextManager
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import io.javalin.http.Handler

object WalletContextManager : WaltIdContextManager() {

    val userContexts: LoadingCache<String, Context> = CacheBuilder.newBuilder().maximumSize(256).build(UserContextLoader)

    fun getUserContext(userInfo: UserInfo) = userContexts.get(userInfo.id)

    val preRequestHandler
        get() = Handler { ctx -> JWTService.getUserInfo(ctx)?.let { setCurrentContext(getUserContext(it)) } }

    val postRequestHandler
        get() = Handler { ctx -> resetCurrentContext() }

}
