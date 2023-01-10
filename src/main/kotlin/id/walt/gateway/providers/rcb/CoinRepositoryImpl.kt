package id.walt.gateway.providers.rcb

import id.walt.gateway.CommonHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*

class CoinRepositoryImpl {
    private val endpoint = "http://socket.walt-test.cloud:9090/api/price/%s"
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    fun findById(id: String): String = let {
        CommonHttp.get(client, String.format(endpoint, id))
    }
}