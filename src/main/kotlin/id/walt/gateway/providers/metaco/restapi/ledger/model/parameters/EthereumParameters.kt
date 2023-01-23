package id.walt.gateway.providers.metaco.restapi.ledger.model.parameters

import kotlinx.serialization.Serializable

@Serializable
data class EthereumParameters(
    val chainId: Int,
    val vaultLedgerIdentifier: String,
    val nativeTickerSymbol: String,
    val nativeTickerName: String,
) : Parameters("Ethereum")
