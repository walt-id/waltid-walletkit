package id.walt.webwallet.backend.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import id.walt.issuer.backend.IssuerManager
import id.walt.services.context.Context
import id.walt.verifier.backend.VerifierManager
import id.walt.webwallet.backend.context.UserContextLoader
import id.walt.webwallet.backend.context.WalletContextManager
import mu.KotlinLogging

class ConfigCmd : CliktCommand(name = "config", help = "Configure or setup dids, keys, etc.") {

    private val log = KotlinLogging.logger {}

    val context: Context by mutuallyExclusiveOptions(
        option("-i", "--as-issuer", help = "Execute in context of issuer backend")
            .convert { tenantId -> IssuerManager.getIssuerContext(tenantId) },
        option("-v", "--as-verifier", help = "Execute in context of verifier backend")
            .convert { tenantId -> VerifierManager.getService().getVerifierContext(tenantId) },
        option("-u", "--as-user", help = "Execute in user context").convert { userId -> UserContextLoader.load(userId) }
    ).single().required()

    override fun run() {
        log.info("Running in context of: $context")
        WalletContextManager.setCurrentContext(context)
    }
}
