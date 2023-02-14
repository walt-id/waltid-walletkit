package id.walt.issuer.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.common.cache.CacheBuilder
import id.walt.multitenancy.TenantState
import javalinjwt.JWTProvider
import java.time.Duration
import java.util.*
import java.util.concurrent.*

class IssuerState : TenantState<IssuerConfig> {
    val nonceCache =
        CacheBuilder.newBuilder().expireAfterWrite(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, Boolean>()
    val sessionCache =
        CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_TIME.seconds, TimeUnit.SECONDS).build<String, IssuanceSession>()

    val authCodeSecret = System.getenv("WALTID_ISSUER_AUTH_CODE_SECRET") ?: UUID.randomUUID().toString()
    val algorithm: Algorithm = Algorithm.HMAC256(authCodeSecret)

    val authCodeProvider = JWTProvider(
        algorithm,
        { session: IssuanceSession, alg: Algorithm? ->
            JWT.create().withSubject(session.id).withClaim("pre-authorized", session.isPreAuthorized).sign(alg)
        },
        JWT.require(algorithm).build()
    )
    var defaultDid: String? = null

    companion object {
        val EXPIRATION_TIME: Duration = Duration.ofMinutes(5)
    }

    override var config: IssuerConfig? = null
}
