package id.walt.webwallet.backend.wallet

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import com.fasterxml.jackson.databind.json.JsonMapper
import id.walt.crypto.Key
import id.walt.issuer.backend.IssuerController
import id.walt.onboarding.backend.OnboardingController
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.verifier.backend.VerifierController
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.plugin.openapi.InitialConfigurationCreator
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.mockk
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import  id.walt.webwallet.backend.auth.UserInfo
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldHaveMinLength

private val log = KotlinLogging.logger {}

class WalletApiTest :  AnnotationSpec() {

    val waltContext = mockk<WalletContextManager>(relaxed = true)
    val host = "localhost"
    val port = 7001
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

    @BeforeAll
    fun init() {
        ServiceMatrix("service-matrix.properties")
        ServiceRegistry.registerService<ContextManager>(waltContext)
    }

    @BeforeClass
    fun startServer() {
        val server = Javalin.create { config ->
            config.apply {
                enableDevLogging()
                requestLogger { ctx, ms ->
                    log.debug { "Received: ${ctx.body()} - Time: ${ms}ms" }
                }
                accessManager(JWTService)
            }
        }.start("127.0.0.1", port)
        server.before(JWTService.jwtHandler)
        server.before(WalletContextManager.preRequestHandler)
        server.after(WalletContextManager.postRequestHandler)

        server.routes {
            ApiBuilder.path("api") {
                AuthController.routes
                WalletController.routes
            }
        }
    }

    @AfterClass
    fun teardown() {
        server?.stop()
    }

    fun authenticate(): UserInfo = runBlocking {
        val userInfo = client.post<UserInfo>("$url/api/auth/login"){
            contentType(ContentType.Application.Json)
            body = mapOf("id" to email, "email" to email, "password" to "1234")
        }
        return@runBlocking userInfo
    }

    @Test()
    fun testLogin() = runBlocking {
        val userInfo = authenticate()
        userInfo.token shouldHaveMinLength 100
        userInfo.id shouldBe email
        userInfo.email shouldBe email
    }

    @Test()
    fun testDidsList() = runBlocking {
        val userInfo = authenticate()
        val did = client.get<String>("$url/api/wallet/did/list"){
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
        }
        println(did)
    }

/*    @Test()
    fun listKeys() = runBlocking {
        val userInfo = authenticate()
        val key = client.get<Key>("$url/wallet/keys/list"){
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
        }
        println(key)
    }*/
}