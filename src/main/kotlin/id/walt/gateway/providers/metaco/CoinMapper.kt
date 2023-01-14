package id.walt.gateway.providers.metaco

import id.walt.gateway.dto.coins.CoinParameter
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker

object CoinMapper {
    fun Ticker.map(currency: String): CoinParameter {
        return when (this.data.name.lowercase()) {
            "bitcoin" -> "bitcoin"
            "ethereum" -> "ethereum"
            "polygon" -> "matic-network"
            "polygon testnet mumbai" -> "polygon testnet mumbai"
            "avalanche coin" -> "avalanche-2"
            "dexalot token" -> "dexalot"
            "bitcoin testnet" -> "t-bitcoin"
            "tezos" -> "tezos"
            "euro test 1" -> "euro"
            "gold test 1" -> "gold"
            else -> ""
//            else -> throw IllegalArgumentException("Missing coin mapping for ticker name.")
        }.let { CoinParameter(it, currency) }
    }
}