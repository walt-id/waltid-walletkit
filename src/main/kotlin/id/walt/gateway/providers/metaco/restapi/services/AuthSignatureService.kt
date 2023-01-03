package id.walt.gateway.providers.metaco.restapi.services

import id.walt.gateway.providers.metaco.ProviderConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class AuthSignatureService : BaseSignatureService<String>() {

    override fun sign(payload: String): String = runBlocking {
        client.post(ProviderConfig.signServiceUrl + "/signatures/string") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("payload" to payload, "privateKey" to ProviderConfig.privateKey))
        }.bodyAsText()
    }
}