package id.walt.gateway.providers.metaco

import id.walt.gateway.dto.CoinParameter
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker

object CoinMapper {
    fun Ticker.map(): CoinParameter {
        return when (this.data.name.lowercase()) {
            "bitcoin" -> "bitcoin"
            "ethereum" -> "ethereum"
            "polygon" -> "matic-network"
            "avalanche coin" -> "avalanche-2"
            "dexalot token" -> "dexalot"
            "bitcoin testnet" -> "t-bitcoin"
            "tezos" -> "tezos"
            else -> throw IllegalArgumentException("Missing coin mapping for ticker name.")
        }.let { CoinParameter(it, "eur") } //TODO: currency is hard-coded -> fix it
    }
}