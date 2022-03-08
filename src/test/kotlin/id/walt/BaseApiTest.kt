package id.walt

import com.fasterxml.jackson.databind.json.JsonMapper
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.auth.UserInfo
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.Javalin
import io.kotest.core.spec.style.AnnotationSpec
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


private val log = KotlinLogging.logger {}

abstract class BaseApiTest : AnnotationSpec() {

    val waltContext = mockk<WalletContextManager>(relaxed = true)
    val host = "localhost"
    val port = 7777
    val url = "http://$host:$port"
    var server: Javalin? = null
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer(
                jackson = JsonMapper.builder()
                    .findAndAddModules()
                    .build()
            ).apply {

            }
        }
        expectSuccess = false
    }
    val email = "test@walt.id"
    val did = "did:web:issuer.ssikit.org"

    @BeforeAll
    fun init() {
        ServiceMatrix("service-matrix.properties")
        ServiceRegistry.registerService<ContextManager>(waltContext)
    }

    @BeforeClass
    fun startServer() {
        server = Javalin.create { config ->
            config.apply {
                enableDevLogging()
                requestLogger { ctx, ms ->
                    log.debug { "Received: ${ctx.body()} - Time: ${ms}ms" }
                }
                accessManager(JWTService)
            }
        }.apply {
            before(JWTService.jwtHandler)
            before(WalletContextManager.preRequestHandler)
            after(WalletContextManager.postRequestHandler)
            routes {
                loadRoutes()
            }
        }.start("127.0.0.1", port)

    }

    abstract fun loadRoutes()

    @AfterClass
    fun teardown() {
        server?.stop()
    }

    fun authenticate(): UserInfo = runBlocking {
        val userInfo = client.post<UserInfo>("$url/api/auth/login") {
            contentType(ContentType.Application.Json)
            body = mapOf(
                "id" to email,
                "email" to email,
                "password" to "1234"
            )
        }
        return@runBlocking userInfo
    }

    fun authenticateDid(): UserInfo = runBlocking {
        val userInfo = client.post<UserInfo>("$url/api/auth/login") {
            contentType(ContentType.Application.Json)
            body = mapOf(
                "id" to did,
                "did" to did
            )
        }
        return@runBlocking userInfo
    }
}