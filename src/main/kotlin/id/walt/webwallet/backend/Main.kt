package id.walt.webwallet.backend

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import id.walt.issuer.backend.IssuerController
import id.walt.issuer.backend.IssuerManager
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.verifier.backend.VerifierController
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.wallet.DidWebRegistryController
import id.walt.webwallet.backend.wallet.WalletController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.plugin.openapi.InitialConfigurationCreator
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import mu.KotlinLogging

internal val WALTID_WALLET_BACKEND_PORT = System.getenv("WALTID_WALLET_BACKEND_PORT")?.toIntOrNull() ?: 8080

internal var WALTID_WALLET_BACKEND_BIND_ADDRESS = System.getenv("WALTID_WALLET_BACKEND_BIND_ADDRESS") ?: "127.0.0.1"

internal val WALTID_DATA_ROOT = System.getenv("WALTID_DATA_ROOT") ?: "."

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) {
    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<ContextManager>(WalletContextManager)

    if (args.isNotEmpty()) when {
        args.contains("--init-issuer") -> {
            IssuerManager.initializeInteractively()
            return
        }
        args.contains("--bind-all") -> WALTID_WALLET_BACKEND_BIND_ADDRESS = "0.0.0.0"
    }

    val app = Javalin.create { config ->
        config.apply {
            enableDevLogging()
            requestLogger { ctx, ms ->
                log.debug { "Received: ${ctx.body()} - Time: ${ms}ms" }
            }
            accessManager(JWTService)
            registerPlugin(RouteOverviewPlugin("/api-routes"))
            registerPlugin(OpenApiPlugin(OpenApiOptions(InitialConfigurationCreator {
                OpenAPI().apply {
                    info {
                        title = "walt.id wallet backend API"
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
                swagger(SwaggerOptions("/api/swagger").title("walt.id wallet backend API"))
                reDoc(ReDocOptions("/api/redoc").title("walt.id wallet backend API"))
            }))
        }
    }.start(WALTID_WALLET_BACKEND_BIND_ADDRESS, WALTID_WALLET_BACKEND_PORT)
    app.before(JWTService.jwtHandler)
    app.before(WalletContextManager.preRequestHandler)
    app.after(WalletContextManager.postRequestHandler)

    app.routes {
        path("api") {
            AuthController.routes
            WalletController.routes
        }
        path("verifier-api") {
            VerifierController.routes
        }
        path("issuer-api") {
            IssuerController.routes
        }
        path("did-registry") {
            DidWebRegistryController.routes
        }
    }

    println("web wallet backend started at: http://$WALTID_WALLET_BACKEND_BIND_ADDRESS:$WALTID_WALLET_BACKEND_PORT")

    println("swagger docs are hosted at: http://$WALTID_WALLET_BACKEND_BIND_ADDRESS:$WALTID_WALLET_BACKEND_PORT/api/swagger")
}
