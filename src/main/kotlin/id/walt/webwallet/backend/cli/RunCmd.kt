package id.walt.webwallet.backend.cli

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import com.beust.klaxon.Klaxon
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.default
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import id.walt.WALTID_WALLET_BACKEND_BIND_ADDRESS
import id.walt.WALTID_WALLET_BACKEND_PORT
import id.walt.issuer.backend.IssuerController
import id.walt.onboarding.backend.OnboardingController
import id.walt.verifier.backend.VerifierController
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.rest.RestAPI
import id.walt.webwallet.backend.wallet.DidWebRegistryController
import id.walt.webwallet.backend.wallet.WalletController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.core.util.RouteOverviewPlugin
import io.javalin.plugin.json.JavalinJackson
import io.javalin.plugin.json.JsonMapper
import io.javalin.plugin.openapi.InitialConfigurationCreator
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import mu.KotlinLogging

class RunCmd : CliktCommand(name = "run", help = "Run wallet backend service") {

  private val log = KotlinLogging.logger {}

  val bindAddress: String by mutuallyExclusiveOptions(
    option("-b", "--bind-address", help = "Bind to address/interface, defaults to env. variable WALTID_WALLET_BACKEND_BIND_ADDRESS: $WALTID_WALLET_BACKEND_BIND_ADDRESS"),
    option("--bind-all", help = "Bind to all interfaces").flag().convert { if(it) "0.0.0.0" ; else null }
  ).single().default(WALTID_WALLET_BACKEND_BIND_ADDRESS)

  val bindPort: Int by option("-p", "--port", help = "Bind to port, defaults to env. variable WALTID_WALLET_BACKEND_PORT: $WALTID_WALLET_BACKEND_PORT").int().default(WALTID_WALLET_BACKEND_PORT)

  override fun run() {
    RestAPI.start(bindAddress, bindPort, JWTService).apply {
      before(JWTService.jwtHandler)
      before(WalletContextManager.preRequestHandler)
      after(WalletContextManager.postRequestHandler)
    }
  }

}
