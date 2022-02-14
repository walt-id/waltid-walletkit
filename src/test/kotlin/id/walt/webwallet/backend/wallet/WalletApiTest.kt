package id.walt.webwallet.backend.wallet

import id.walt.BaseApiTest
import id.walt.webwallet.backend.auth.AuthController
import io.javalin.apibuilder.ApiBuilder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMinLength
import io.ktor.client.request.*
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