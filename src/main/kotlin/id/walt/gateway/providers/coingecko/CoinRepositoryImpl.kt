package id.walt.gateway.providers.coingecko

import id.walt.gateway.CommonHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*

class CoinRepositoryImpl : CoinRepository {
    private val endpoint =
        "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=%s&include_market_cap=true&include_24hr_change=true"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    private fun findById(vararg ids: String, currency: String) =
        CommonHttp.get(client, String.format(endpoint, ids.joinToString(","), currency))

    override fun findById(id: String, currency: String) = this.findById(ids = arrayOf(id), currency)
}