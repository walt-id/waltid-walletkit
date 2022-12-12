package id.walt.gateway.providers.metaco.restapi.signservice

import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class IntentSignatureService : BaseSignatureService<NoSignatureIntent>() {

    override fun sign(payload: NoSignatureIntent): String = runBlocking {
        client.post(ProviderConfig.signServiceUrl + "/signatures/payload") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("payload" to payload, "privateKey" to ProviderConfig.privateKey))
        }.bodyAsText()
    }
}