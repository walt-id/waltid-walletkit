package id.walt.webwallet.backend.cli

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
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.rest.RestAPI
import mu.KotlinLogging

class RunCmd : CliktCommand(name = "run", help = "Run walletkit service") {

    private val log = KotlinLogging.logger {}

    val bindAddress: String by mutuallyExclusiveOptions(
        option(
            "-b",
            "--bind-address",
            help = "Bind to address/interface, defaults to env. variable WALTID_WALLET_BACKEND_BIND_ADDRESS: $WALTID_WALLET_BACKEND_BIND_ADDRESS"
        ),
        option("--bind-all", help = "Bind to all interfaces").flag().convert { if (it) "0.0.0.0"; else null }
    ).single().default(WALTID_WALLET_BACKEND_BIND_ADDRESS)

    val bindPort: Int by option(
        "-p",
        "--port",
        help = "Bind to port, defaults to env. variable WALTID_WALLET_BACKEND_PORT: $WALTID_WALLET_BACKEND_PORT"
    ).int().default(WALTID_WALLET_BACKEND_PORT)

    override fun run() {
        RestAPI.start(bindAddress, bindPort, JWTService).apply {
            before(JWTService.jwtHandler)
            before(WalletContextManager.preRequestHandler)
            after(WalletContextManager.postRequestHandler)
        }
    }

}
