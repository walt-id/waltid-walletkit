package id.walt

import cc.vileda.openapi.dsl.components
import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.security
import com.beust.klaxon.Klaxon
import com.github.ajalt.clikt.core.subcommands
import id.walt.cli.*
import id.walt.issuer.backend.IssuerController
import id.walt.issuer.backend.IssuerManager
import id.walt.onboarding.backend.OnboardingController
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.verifier.backend.VerifierController
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.cli.ConfigCmd
import id.walt.webwallet.backend.cli.RunCmd
import id.walt.webwallet.backend.cli.WalletCmd
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.wallet.DidWebRegistryController
import id.walt.webwallet.backend.wallet.WalletController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
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

val WALTID_WALLET_BACKEND_PORT = System.getenv("WALTID_WALLET_BACKEND_PORT")?.toIntOrNull() ?: 8080

var WALTID_WALLET_BACKEND_BIND_ADDRESS = System.getenv("WALTID_WALLET_BACKEND_BIND_ADDRESS") ?: "127.0.0.1"

val WALTID_DATA_ROOT = System.getenv("WALTID_DATA_ROOT") ?: "."


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

    WalletCmd().subcommands(
        RunCmd(),
        ConfigCmd().subcommands(
            KeyCommand().subcommands(
                GenKeyCommand(),
                ListKeysCommand(),
                ImportKeyCommand(),
                ExportKeyCommand()
            ),
            DidCommand().subcommands(
                CreateDidCommand(),
                ResolveDidCommand(),
                ListDidsCommand(),
                ImportDidCommand()
            ),
            EssifCommand().subcommands(
                EssifOnboardingCommand(),
                EssifAuthCommand(),
//                        EssifVcIssuanceCommand(),
//                        EssifVcExchangeCommand(),
                EssifDidCommand().subcommands(
                    EssifDidRegisterCommand()
                )
            ),
            VcCommand().subcommands(
                VcIssueCommand(),
                PresentVcCommand(),
                VerifyVcCommand(),
                ListVcCommand(),
                ListVerificationPoliciesCommand(),
                VcTemplatesCommand().subcommands(
                    VcTemplatesListCommand(),
                    VcTemplatesExportCommand()
                ),
                VcImportCommand()
            )
        )
    ).main(args)
}
