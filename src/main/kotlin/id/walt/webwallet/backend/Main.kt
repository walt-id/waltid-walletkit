package id.walt.webwallet.backend

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import cc.vileda.openapi.dsl.securityScheme
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.WaltContext
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.wallet.WalletController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.plugin.openapi.InitialConfigurationCreator
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import javalinjwt.JWTAccessManager

fun main(args: Array<String>) {
    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<WaltContext>(WalletContextManager)

    val app = Javalin.create{ config ->
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
    }.start(8080)
    app.before(JWTService.jwtHandler)
    app.before(WalletContextManager.preRequestHandler)
    app.after(WalletContextManager.postRequestHandler)

    app.routes {
        path("api") {
            AuthController.routes
            WalletController.routes
        }
    }
}
