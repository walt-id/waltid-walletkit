package id.walt.multitenancy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import id.walt.issuer.backend.IssuerConfig
import id.walt.issuer.backend.IssuerTenant
import id.walt.webwallet.backend.context.WalletContextManager
import java.io.File

class TenantCmd : CliktCommand(name = "tenant", help = "Manage tenant for this issuer or verifier") {

    override fun run() {
    }
}

class ConfigureTenantCmd : CliktCommand(name = "configure", help = "Configure current issuer or verifier tenant") {
    val config: String by argument("config", help = "Path to config file for this tenant")

    override fun run() {
        val configFile = File(config)
        if (!configFile.exists()) {
            throw Exception("Config file not found")
        }

        val tenantContext = WalletContextManager.currentContext as TenantContext<*, *>
        when (tenantContext.tenantId.type) {
            TenantType.ISSUER -> IssuerTenant.setConfig(IssuerConfig.fromJson(configFile.readText()))
            else -> throw IllegalArgumentException("Tenant type not yet supported")
        }
    }
}
