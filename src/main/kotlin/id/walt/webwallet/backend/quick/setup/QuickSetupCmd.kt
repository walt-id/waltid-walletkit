package id.walt.webwallet.backend.quick.setup

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import id.walt.common.KlaxonWithConverters

class QuickSetupCmd : CliktCommand(
    name = "quick-setup",
    help = "Perform a quick-setup. Create the issuer and verifier tenants and create a did for each"
) {

    override fun run() {
    }
}

class QuickSetupRunCmd : CliktCommand(name = "run", help = "Run the quick-setup") {
    private val hosts: List<String> by argument().multiple()

    override fun run() {
        echo("Running the quick-setup..")
        val config = QuickSetup.run(hosts)
        echo("Configuration result:")
        echo(KlaxonWithConverters().toJsonString(config))
    }
}