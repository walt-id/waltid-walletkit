package id.walt.gateway.providers.metaco.restapi.services

import com.beust.klaxon.Klaxon
import id.walt.gateway.providers.metaco.ProviderConfig
import id.walt.gateway.providers.metaco.restapi.intent.model.NoSignatureIntent
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class IntentSignatureService : BaseSignatureService<NoSignatureIntent>() {

    override fun sign(payload: NoSignatureIntent): String = runBlocking {
        val result = client.post(ProviderConfig.signServiceUrl + "/signatures/key") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("payload" to Klaxon().toJsonString(payload), "privateKey" to ProviderConfig.privateKey))
        }.bodyAsText()
        result
    }
}