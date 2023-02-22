package id.walt.webwallet.backend.rest

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import id.walt.gateway.routers.GatewayRouter
import id.walt.issuer.backend.IssuerController
import id.walt.multitenancy.MultitenancyController
import id.walt.multitenancy.Tenant.TenantNotFoundException
import id.walt.onboarding.backend.OnboardingController
import id.walt.verifier.backend.VerifierController
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.wallet.DidWebRegistryController
import id.walt.webwallet.backend.wallet.WalletController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.core.security.AccessManager
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.json.JavalinJackson
import io.javalin.plugin.json.JsonMapper
import io.javalin.plugin.openapi.InitialConfigurationCreator
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.ktor.http.*
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import mu.KotlinLogging
import java.security.InvalidKeyException

object RestAPI {

    private val log = KotlinLogging.logger {}

    val DEFAULT_ROUTES = {
        path("api") {
            AuthController.routes
            WalletController.routes
            DidWebRegistryController.routes
            GatewayRouter.routes()
            MultitenancyController.routes
        }
        path("verifier-api") {
            VerifierController.routes
        }
        path("issuer-api") {
            IssuerController.routes
        }
        path("onboarding-api") {
            OnboardingController.routes
        }
    }

    var apiTitle = "walt.id walletkit API"

    fun createJavalin(accessManager: AccessManager): Javalin = Javalin.create { config ->
        config.apply {
            //enableDevLogging()
            enableCorsForAllOrigins()
            requestLogger { ctx, ms ->
                if (ctx.url().endsWith("isVerified")) return@requestLogger

                log.debug {
                    StringBuilder("HTTP: ${ctx.method()} ${ctx.fullUrl()} - ${ctx.status()} in ${ms}ms")
                        .apply {
                            if (ctx.body().isNotEmpty()) append("\nRequest: ${ctx.body()}")
                            if (!ctx.resultString().isNullOrEmpty()) append("\nResponse: ${ctx.resultString()}")

                            val location = ctx.res.getHeader(HttpHeaders.Location)
                            if (!location.isNullOrEmpty()) append("\nLocation: $location")
                        }.toString()
                }
            }
            accessManager(accessManager)
            registerPlugin(RouteOverviewPlugin("/api-routes"))
            registerPlugin(OpenApiPlugin(OpenApiOptions(InitialConfigurationCreator {
                OpenAPI().apply {
                    info {
                        title = apiTitle
                    }
                    servers = listOf(Server().url("/"))
                    components {
                        addSecuritySchemes("bearerAuth", SecurityScheme().apply {
                            name = "bearerAuth"
                            type = SecurityScheme.Type.HTTP
                            scheme = "bearer"
                            `in` = SecurityScheme.In.HEADER
                            bearerFormat = "JWT"
                        })
                    }
                    security {
                        addList("bearerAuth")
                    }
                }
            }).apply {
                path("/api/api-documentation")
                swagger(SwaggerOptions("/api/swagger").title(apiTitle))
                reDoc(ReDocOptions("/api/redoc").title(apiTitle))
            }))

            this.jsonMapper(object : JsonMapper {
                override fun toJsonString(obj: Any): String {
                    return Klaxon().toJsonString(obj)
                }

                override fun <T : Any?> fromJsonString(json: String, targetClass: Class<T>): T & Any {
                    return JavalinJackson().fromJsonString(json, targetClass)
                        ?: throw IllegalArgumentException("Cannot deserialize JSON: $json")
                }
            })
        }
    }.apply {

        fun Context.reportRequestException(exception: Exception): Context {
            exception.printStackTrace()
            return this.json(
                mapOf(
                    "error" to true,
                    "error_type" to exception::class.simpleName,
                    "message" to exception.message,
                    "url" to this.fullUrl(),
                    "stacktrace" to exception.stackTraceToString()
                )
            )
                .status(HttpCode.BAD_REQUEST)
        }

        //exception(IllegalArgumentException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(MismatchedInputException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(JsonParseException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(KlaxonException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(InvalidKeyException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(java.security.spec.InvalidKeySpecException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(TenantNotFoundException::class.java) { e, ctx -> ctx.reportRequestException(e) }
        exception(Exception::class.java) { e, ctx -> ctx.reportRequestException(e) }
        //exception(Tenant.WaltContextTenantSystemError::class.java) { e, ctx -> ctx.reportRequestException(e) }
    }

    fun start(bindAddress: String, port: Int, accessManager: AccessManager, routes: () -> Unit = DEFAULT_ROUTES): Javalin {
        val javalin = createJavalin(accessManager)
        javalin.routes(routes)
        javalin.start(bindAddress, port)
        println("web walletkit started at: http://$bindAddress:$port")
        println("swagger docs are hosted at: http://$bindAddress:$port/api/swagger")
        return javalin
    }
}
