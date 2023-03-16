package id.walt

import com.github.ajalt.clikt.core.subcommands
import id.walt.cli.*
import id.walt.multitenancy.ConfigureTenantCmd
import id.walt.multitenancy.TenantCmd
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.webwallet.backend.cli.ConfigCmd
import id.walt.webwallet.backend.cli.RunCmd
import id.walt.webwallet.backend.cli.WalletCmd
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.quick.setup.QuickSetupCmd
import id.walt.webwallet.backend.quick.setup.QuickSetupRunCmd
import kotlinx.coroutines.runBlocking


val WALTID_WALLET_BACKEND_PORT = System.getenv("WALTID_WALLET_BACKEND_PORT")?.toIntOrNull() ?: 8080
var WALTID_WALLET_BACKEND_BIND_ADDRESS = System.getenv("WALTID_WALLET_BACKEND_BIND_ADDRESS") ?: "0.0.0.0"

val WALTID_DATA_ROOT = System.getenv("WALTID_DATA_ROOT") ?: "."

fun main(args: Array<String>): Unit = runBlocking {

    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<ContextManager>(WalletContextManager)

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
                VerificationPoliciesCommand().subcommands(
                    ListVerificationPoliciesCommand(),
                    CreateDynamicVerificationPolicyCommand(),
                    RemoveDynamicVerificationPolicyCommand()
                ),
                VcTemplatesCommand().subcommands(
                    VcTemplatesListCommand(),
                    VcTemplatesImportCommand(),
                    VcTemplatesExportCommand(),
                    VcTemplatesRemoveCommand()
                ),
                VcImportCommand()
            ),
            TenantCmd().subcommands(
                ConfigureTenantCmd()
            ),
            ServeCommand(),
        ),
        QuickSetupCmd().subcommands(
            QuickSetupRunCmd()
        ),
    ).main(args)
}
