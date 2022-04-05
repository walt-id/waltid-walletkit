package id.walt.webwallet.backend.wallet

import id.walt.BaseApiTest
import id.walt.crypto.KeyAlgorithm
import id.walt.services.key.KeyService
import id.walt.webwallet.backend.auth.AuthController
import io.javalin.apibuilder.ApiBuilder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMinLength
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class WalletApiTest : BaseApiTest() {

    override fun loadRoutes() {
        ApiBuilder.path("api") {
            AuthController.routes
            WalletController.routes
        }
    }

    @Test()
    fun testLogin() = runBlocking {
        val userInfo = authenticate()
        userInfo.token shouldHaveMinLength 100
        userInfo.id shouldBe email
        userInfo.email shouldBe email
    }

    @Test()
    fun testDidsList() = runBlocking {
        val userInfo = authenticate()
        val did = client.get<String>("$url/api/wallet/did/list") {
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
        }
        println(did)
    }

    // TODO: analyze potential walt-context issue @Test()
    fun testDidWebCreate() = runBlocking {
        val userInfo = authenticateDid()
        val did = client.post<String>("$url/api/wallet/did/create"){
            header("Authorization", "Bearer ${userInfo.token}")
            accept(ContentType("plain", "text"))
            contentType(ContentType.Application.Json)
            body = mapOf("method" to "web", "didWebDomain" to null)
        }
        did shouldStartWith "did:web"

        println(did)
    }

    @Test
    fun testDeleteKey() {
        val userInfo = authenticate()
        val context = waltContext.getUserContext(userInfo)
        val kid = waltContext.runWith(context) { KeyService.getService().generate(KeyAlgorithm.EdDSA_Ed25519) }
        val response = runBlocking {
            client.delete<HttpResponse>("$url/api/wallet/keys/delete") {
                header("Authorization", "Bearer ${userInfo.token}")
                body = kid.id
            }
        }
        response.status shouldBe HttpStatusCode.OK
        waltContext.runWith(context) {
            shouldThrow<Exception> {
                KeyService.getService().load(kid.id)
            }
        }
    }

/*    @Test()
    fun listKeys() = runBlocking {
        val userInfo = authenticate()
        val key = client.get<Key>("$url/wallet/keys/list"){
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
        }
        println(key)
    }*/
}