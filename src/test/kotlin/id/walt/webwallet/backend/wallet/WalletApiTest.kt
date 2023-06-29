package id.walt.webwallet.backend.wallet

import id.walt.BaseApiTest
import id.walt.crypto.KeyAlgorithm
import id.walt.rest.custodian.ExportKeyRequest
import id.walt.services.key.KeyFormat
import id.walt.services.key.KeyService
import id.walt.services.keystore.KeyType
import id.walt.webwallet.backend.auth.AuthController
import id.walt.webwallet.backend.auth.UserInfo
import io.javalin.apibuilder.ApiBuilder.path
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMinLength
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class WalletApiTest : BaseApiTest() {

    override fun loadRoutes() {
        path("api") {
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
        val did = client.get("$url/api/wallet/did/list") {
            header("Authorization", "Bearer ${userInfo.token}")
            contentType(ContentType.Application.Json)
        }.bodyAsText()
        println(did)
    }

    // TODO: analyze potential walt-context issue @Test()
    fun testDidWebCreate() = runBlocking {
        val userInfo = authenticate(didBody)
        val did = client.post("$url/api/wallet/did/create") {
            header("Authorization", "Bearer ${userInfo.token}")
            accept(ContentType("plain", "text"))
            contentType(ContentType.Application.Json)
            setBody(mapOf("method" to "web", "didWebDomain" to null))
        }.bodyAsText()
        did shouldStartWith "did:web"

        println(did)
    }

    @Test
    fun testDeleteKey() {
        val userInfo = authenticate()
        val context = waltContext.getUserContext(userInfo)
        val kid = waltContext.runWith(context) { KeyService.getService().generate(KeyAlgorithm.EdDSA_Ed25519) }
        val response = runBlocking {
            client.delete("$url/api/wallet/keys/delete/${kid.id}") {
                header("Authorization", "Bearer ${userInfo.token}")
            }
        }
        response.status shouldBe HttpStatusCode.OK
        waltContext.runWith(context) {
            shouldThrow<Exception> {
                KeyService.getService().load(kid.id)
            }
        }
    }

    @Test
    fun testExportKey() {
        forAll(
            row(KeyAlgorithm.EdDSA_Ed25519, KeyFormat.JWK, true),
            row(KeyAlgorithm.ECDSA_Secp256k1, KeyFormat.JWK, true),
            row(KeyAlgorithm.RSA, KeyFormat.JWK, true),
            row(KeyAlgorithm.EdDSA_Ed25519, KeyFormat.PEM, true),
            row(KeyAlgorithm.ECDSA_Secp256k1, KeyFormat.PEM, true),
            row(KeyAlgorithm.RSA, KeyFormat.PEM, true),
            row(KeyAlgorithm.EdDSA_Ed25519, KeyFormat.JWK, false),
            row(KeyAlgorithm.ECDSA_Secp256k1, KeyFormat.JWK, false),
            row(KeyAlgorithm.RSA, KeyFormat.JWK, false),
            row(KeyAlgorithm.EdDSA_Ed25519, KeyFormat.PEM, false),
            row(KeyAlgorithm.ECDSA_Secp256k1, KeyFormat.PEM, false),
            row(KeyAlgorithm.RSA, KeyFormat.PEM, false),
        ) { alg, format, private ->
            val userInfo = authenticate()
            val context = waltContext.getUserContext(userInfo)
            val kid = waltContext.runWith(context) { KeyService.getService().generate(alg) }
            val exportRequest = ExportKeyRequest(kid.id, format, private)
            val keyStr = waltContext.runWith(context) {
                KeyService.getService().export(
                    kid.id,
                    format,
                    if (private) KeyType.PRIVATE else KeyType.PUBLIC
                )
            }
            val response = runBlocking {
                client.post("$url/api/wallet/keys/export") {
                    header("Authorization", "Bearer ${userInfo.token}")
                    contentType(ContentType.Application.Json)
                    setBody(exportRequest)
                }.bodyAsText()
            }
            println(response)
            response shouldBe keyStr
        }
    }

    @Test
    fun testPropertySelectionOnAuthenticate(){
        forAll(
            row(emailBody, UserInfo(id = emailBody["id"]!!).also { it.email = emailBody["email"] }),
            row(didBody, UserInfo(id = didBody["id"]!!).also { it.did = didBody["did"] }),
            row(mapOf("id" to "eth##123"), UserInfo(id = "123").also { it.ethAccount = "123" }),
            row(mapOf("id" to "tz123"), UserInfo(id = "tz123").also { it.tezosAccount = "tz123" }),
            row(mapOf("id" to "pol##123"), UserInfo(id = "123").also { it.polkadotAccount = "123" }),
            row(mapOf("id" to "polevm##123"), UserInfo(id = "123").also { it.polkadotEvmAccount = "123" }),
            row(mapOf("id" to "flow##123"), UserInfo(id = "123").also { it.flowAccount = "123" }),
            row(mapOf("id" to "near##did.near"), UserInfo(id = "did.near").also { it.nearAccount = "did.near" }),
//            row(mapOf("id" to "did.near"), UserInfo(id = "did.near").also { it.nearAccount = "did.near" }),
        ){ body, expected ->
            val userInfo = authenticate(body)
            userInfo shouldBe expected
        }
    }
}
