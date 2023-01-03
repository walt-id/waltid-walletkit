package id.walt.issuer.backend

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.common.cache.CacheBuilder
import id.walt.model.DidMethod
import id.walt.services.did.DidService
import id.walt.webwallet.backend.context.WalletContextManager
import javalinjwt.JWTProvider
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class IssuerState {
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

  companion object {
    val EXPIRATION_TIME: Duration = Duration.ofMinutes(5)
  }
}