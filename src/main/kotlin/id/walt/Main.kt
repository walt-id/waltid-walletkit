package id.walt

import com.github.ajalt.clikt.core.subcommands
import id.walt.cli.*
import id.walt.issuer.backend.IssuerManager
import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.socket.Server
import id.walt.socket.StoreParameter
import id.walt.webwallet.backend.cli.ConfigCmd
import id.walt.webwallet.backend.cli.RunCmd
import id.walt.webwallet.backend.cli.WalletCmd
import id.walt.webwallet.backend.context.WalletContextManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


val WALTID_WALLET_BACKEND_PORT = System.getenv("WALTID_WALLET_BACKEND_PORT")?.toIntOrNull() ?: 8080
val WALTID_WALLET_SOCKET_PORT = System.getenv("WALTID_WALLET_SOCKET_PORT")?.toIntOrNull() ?: 9999

var WALTID_WALLET_BACKEND_BIND_ADDRESS = System.getenv("WALTID_WALLET_BACKEND_BIND_ADDRESS") ?: "0.0.0.0"

val WALTID_DATA_ROOT = System.getenv("WALTID_DATA_ROOT") ?: "."

val WALTID_TLS_VERSION = System.getenv("WALTID_TLS_VERSION") ?: "TLSv1.2"
val WALTID_TRUSTSTORE_PATH = System.getenv("WALTID_TRUSTSTORE_PATH") ?: "servercert.p12"
val WALTID_KEYSTORE_PATH = System.getenv("WALTID_KEYSTORE_PATH") ?: "servercert.p12"
val WALTID_TRUSTSTORE_PWD = (System.getenv("WALTID_TRUSTSTORE_PWD") ?: "").toCharArray()
val WALTID_KEYSTORE_PWD = (System.getenv("WALTID_KEYSTORE_PWD") ?: "").toCharArray()


fun main(args: Array<String>): Unit = runBlocking {

    launch {
        Server().start(
            WALTID_WALLET_SOCKET_PORT,
            StoreParameter(WALTID_KEYSTORE_PATH, WALTID_KEYSTORE_PWD),
            StoreParameter(WALTID_TRUSTSTORE_PATH, WALTID_TRUSTSTORE_PWD),
            WALTID_TLS_VERSION,
        )
    }

    ServiceMatrix("service-matrix.properties")
    ServiceRegistry.registerService<ContextManager>(WalletContextManager)

    if (args.isNotEmpty()) when {
        args.contains("--init-issuer") -> {
            IssuerManager.initializeInteractively()
            return@runBlocking
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
                VerificationPoliciesCommand().subcommands(
                    ListVerificationPoliciesCommand(),
                    CreateDynamicVerificationPolicyCommand(),
                    RemoveDynamicVerificationPolicyCommand()
                ),
                VcTemplatesCommand().subcommands(
                    VcTemplatesListCommand(),
                    VcTemplatesExportCommand(),
                    VcTemplatesImportCommand(),
                    VcTemplatesRemoveCommand()
                ),
                VcImportCommand()
            )
        )
    ).main(args)
}
