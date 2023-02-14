package id.walt.gateway.providers.goldorg

import com.beust.klaxon.Klaxon
import id.walt.gateway.Common
import id.walt.gateway.CommonHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class HistoricalPriceRepositoryImpl: HistoricalPriceRepository {
    val endpoint = "https://fsapi.gold.org/api/goldprice/v11/chart/price/eur/grams/%s,%s"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    override fun get(timeframe: String): Map<String, String> = Common.timeframeToTimestamp(timeframe).let {
        val result = CommonHttp.get(client, String.format(endpoint, it.first, it.second))
        Klaxon().parse<Map<String, Any>>(result)?.let {
            (it["chartData"] as? Map<*, *>)?.get("EUR")?.let {
                (it as? List<List<Double>> ?: emptyList())
            }
        }
    }?.associate {
        Instant.ofEpochMilli(it[0].toLong()).truncatedTo(ChronoUnit.SECONDS).toString() to it[1].toString()
    } ?: emptyMap()
}