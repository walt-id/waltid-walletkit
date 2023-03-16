package id.walt.webwallet.backend.quick.setup

import com.github.ajalt.clikt.core.CliktCommand
import id.walt.common.KlaxonWithConverters

class QuickSetupCmd : CliktCommand(
    name = "quick-setup",
    help = "Perform a quick-setup. Create the issuer and verifier tenants and create a did for each"
) {

    override fun run() {
    }
}

class QuickSetupRunCmd : CliktCommand(name = "run", help = "Run the quick-setup") {

    override fun run() {
        echo("Running the quick-setup..")
        val config = QuickSetup.run()
        echo("Configuration result:")
        echo(KlaxonWithConverters().toJsonString(config))
    }
}