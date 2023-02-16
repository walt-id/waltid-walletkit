package id.walt.gateway.providers.metaco

import id.walt.gateway.dto.coins.CoinParameter
import id.walt.gateway.providers.metaco.restapi.ticker.model.Ticker
import java.io.File

object CoinMapper {
    val map: Map<String, String> by lazy { loadMap(ProviderConfig.coinMapPath) }
    fun Ticker.map(currency: String): CoinParameter = mapNameToCoinParameter(this.data.name, currency)

    fun mapNameToCoinParameter(name: String, currency: String) = CoinParameter(map[name.lowercase()] ?: "", currency)

    private fun loadMap(filepath: String): Map<String, String> = File(filepath).takeIf { it.exists() }?.let {
        it.readLines().associate {
            it.split(",").takeIf { it.size > 1 }?.let {
                Pair(it[0].lowercase(), it[1])
            } ?: Pair("", "")
        }.filter { it.key.isNotEmpty() }
    } ?: emptyMap()
}