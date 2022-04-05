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
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


private val log = KotlinLogging.logger {}

abstract class BaseApiTest : AnnotationSpec() {

    val waltContext = WalletContextManager
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

        val userInfo = UserInfo(email)
        userInfo.email = email
        userInfo.password = "1234"
        userInfo.token = "e4c98176b8acc069d87e40c7f673aa493eea05e766624843b9d2c3f99bf7af25"
//        every { waltContext.preRequestHandler } returns Handler {
//            WalletContextManager.setCurrentContext(
//                WalletContextManager.getUserContext(userInfo)
//            )
//        }
//
//        every { waltContext.postRequestHandler } returns Handler {
//            WalletContextManager.setCurrentContext(
//                WalletContextManager.getUserContext(userInfo)
//            )
//        }
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
            before(waltContext.preRequestHandler)
            after(waltContext.postRequestHandler)
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
