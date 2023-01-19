package id.walt.webwallet.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.javalin.core.security.AccessManager
import io.javalin.core.security.RouteRole
import io.javalin.http.Context
import io.javalin.http.Handler
import javalinjwt.JWTProvider
import javalinjwt.JavalinJWT
import java.util.*


object JWTService : AccessManager {

    val secret = System.getenv("WALTID_WALLET_AUTH_SECRET") ?: UUID.randomUUID().toString()
    val algorithm: Algorithm = Algorithm.HMAC256(secret)

    val provider = JWTProvider(
        algorithm,
        { user: UserInfo, alg: Algorithm? ->
            JWT.create().withSubject(user.id).sign(alg)
        },
        JWT.require(algorithm).build()
    )

    val jwtHandler: Handler
        get() = JavalinJWT.createHeaderDecodeHandler(provider)

    fun toJWT(user: UserInfo): String {
        return provider.generateToken(user)
    }

    fun fromJwt(jwt: DecodedJWT): UserInfo {
        return UserInfo(jwt.subject).apply {
            token = jwt.token
        }
    }

    fun getUserInfo(ctx: Context): UserInfo? = when (JavalinJWT.containsJWT(ctx)) {
        true -> fromJwt(JavalinJWT.getDecodedFromContext(ctx))
        else -> null
    }

    override fun manage(handler: Handler, ctx: Context, routeRoles: MutableSet<RouteRole>) {
        // if context contains decoded JWT, it was already validated by jwtHandler
        if (
            (JavalinJWT.containsJWT(ctx) && routeRoles.contains(UserRole.AUTHORIZED))
            || !routeRoles.contains(UserRole.AUTHORIZED)
        ) {
            handler.handle(ctx)
        } else {
            ctx.status(401).result("Unauthorized")
        }
    }
}
