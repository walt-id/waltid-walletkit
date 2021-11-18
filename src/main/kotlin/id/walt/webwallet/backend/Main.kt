package id.walt.webwallet.backend

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import id.walt.issuer.backend.IssuerController
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.verifier.backend.VerifierController
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
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

internal const val WALLET_BACKEND_PORT = 8080

internal const val WALLET_BACKEND_BIND_ADDRESS = "127.0.0.1"

internal val WALTID_DATA_ROOT = System.getenv("WALTID_DATA_ROOT") ?: "."

fun main(args: Array<String>) {
    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<ContextManager>(WalletContextManager)

    val app = Javalin.create { config ->
        config.apply {
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
    }.start(WALLET_BACKEND_BIND_ADDRESS, WALLET_BACKEND_PORT)
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
    }

    println("web wallet backend started at: http://$WALLET_BACKEND_BIND_ADDRESS:$WALLET_BACKEND_PORT")

    println("swagger docs are hosted at: http://$WALLET_BACKEND_BIND_ADDRESS:$WALLET_BACKEND_PORT/api/swagger")
}
