package id.walt.gateway.providers.metaco.restapi

import com.beust.klaxon.Klaxon
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

object CommonHttp {
    inline fun <reified T> get(
        client: HttpClient,
        endpoint: String,
        body: Map<String, String> = emptyMap()
    ) = runBlocking {
//        val response = client.get("$baseUrl$endpoint") {
        val response = client.get(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyAsText()
        Klaxon().parse<T>(response)!!
    }

    fun buildUrl(vararg paths: String) = paths.joinToString {
        val item = if (it.startsWith("/")) it.substring(1) else it
        if (item.endsWith("/")) item else item.plus("/")
    }

    fun buildQueryList(params: Map<String, String>) = params.map {
        "&${it.key}=${it.value}"
    }.joinToString { it }.replaceFirst("&", "?")
}