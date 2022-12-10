package id.walt.gateway

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
    ) = runBlocking {
        val response = get(client, endpoint)
        Klaxon().parse<T>(response)!!
    }

    fun get(
        client: HttpClient,
        endpoint: String,
    ) = runBlocking {
        client.get(endpoint).bodyAsText()
    }

    //TODO: class polymorphism for ktor client serialization doesn't work
//    inline fun <reified T> post(
//        client: HttpClient,
//        endpoint: String,
//        body: Any,
//    ) = runBlocking {
//        val response = post(client, endpoint, body)
//        Klaxon().parse<T>(response)!!
//    }
//    fun post(
//        client: HttpClient,
//        endpoint: String,
//        body: Any
//    ) = runBlocking {
//        client.post(endpoint) {
//            contentType(ContentType.Application.Json)
//            setBody(body)
//        }.bodyAsText()
//    }

    fun post(
        client: HttpClient,
        endpoint: String,
        body: String,
    ) = runBlocking {
        client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.bodyAsText()
    }

    fun buildUrl(vararg paths: String) = paths.joinToString("") {
        val item = if (it.startsWith("/")) it.substring(1) else it
        if (item.endsWith("/")) item else item.plus("/")
    }.removeSuffix("/")

    fun buildQueryList(params: Map<String, String>) = params.map {
        "&${it.key}=${it.value}"
    }.joinToString { it }.replaceFirst("&", "?")
}