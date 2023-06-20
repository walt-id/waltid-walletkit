package id.walt.webwallet.backend.wallet

import id.walt.servicematrix.ServiceMatrix
import id.walt.servicematrix.ServiceRegistry
import id.walt.services.context.ContextManager
import id.walt.webwallet.backend.auth.JWTService
import id.walt.webwallet.backend.context.WalletContextManager
import id.walt.webwallet.backend.rest.RestAPI
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import java.util.*

class End2End : StringSpec({

    val http = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url("http://127.0.0.1:8080/")
            contentType(ContentType.Application.Json)
        }
    }
    "Init ServiceMatrix & ServiceRegistry" {
        ServiceMatrix("service-matrix.properties")
        ServiceRegistry.registerService<ContextManager>(WalletContextManager)
    }
    "Start server" {
        RestAPI.start("127.0.0.1", 8080, JWTService).apply {
            before(JWTService.jwtHandler)
            before(WalletContextManager.preRequestHandler)
            after(WalletContextManager.postRequestHandler)
        }
    }
    "Check OpenAPI docs" {
        http.get("api/swagger").status.isSuccess() shouldBe true
        http.get("api/api-documentation")
            .bodyAsText() shouldContain "\"/api-routes\":{\"get\":{\"summary\":\"Get apiRoutes\",\"operationId\":\"getApiRoutes\""
    }

    val uid = UUID.randomUUID().toString() + "@test.example"

    lateinit var token: String
    "Login" {
        shouldNotThrowAny {
            token = http.post("api/auth/login") {
                setBody(mapOf("id" to uid))
            }.body<JsonObject>()["token"]?.jsonPrimitive?.content ?: throw IllegalArgumentException("No token in login response")
        }
    }

    val authenticatedHttp by lazy {
        HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json()
            }
            defaultRequest {
                url("http://127.0.0.1:8080/")
                contentType(ContentType.Application.Json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(token, token)
                    }
                }
            }
        }
    }


    lateinit var did: String
    "DID list" {
        authenticatedHttp.get("api/wallet/did/list").body<List<String>>().let {
            it.size shouldBe 1
            it.first() shouldStartWith "did:key:"
            did = it.first()
        }
    }

    lateinit var initiateIssuanceUrl: String
    "Issuance request" {
        initiateIssuanceUrl = http.post("issuer-api/default/credentials/issuance/request") {
            parameter("tenantId", "default")
            parameter("walletId", "x-device")
            parameter("isPreAuthorized", true)

            setBody(
                mapOf(
                    "credentials" to
                            listOf(mapOf("type" to "VerifiableId"))
                )
            )
        }.also { it.status.isSuccess() shouldBe true }.bodyAsText()
    }

    lateinit var issuerSessionId: String
    "Use issuance request" {
        issuerSessionId = authenticatedHttp.post("api/wallet/issuance/startIssuerInitiatedIssuance") {
            setBody(mapOf("oidcUri" to initiateIssuanceUrl))
        }.also { it.status.isSuccess() shouldBe true }.bodyAsText()
    }

    "Info issuance" {
        authenticatedHttp.get("api/wallet/issuance/info") {
            parameter("sessionId", issuerSessionId)
        }.body<JsonObject>().let {
            it["credentialTypes"]!!.jsonArray.let { credentialTypes ->
                credentialTypes shouldHaveSize 1
                credentialTypes.first().jsonPrimitive.content shouldBe "VerifiableId"
            }
            it["id"]?.jsonPrimitive?.content shouldBe issuerSessionId
            it["credentials"]!!.jsonPrimitive.contentOrNull shouldBe null
        }
    }

    "Continue issuance" {
        authenticatedHttp.get("api/wallet/issuance/continueIssuerInitiatedIssuance") {
            parameter("sessionId", issuerSessionId)
            parameter("did", did)
        }.status.isSuccess() shouldBe true
    }

    "Info issuance 2" {
        authenticatedHttp.get("api/wallet/issuance/info") {
            parameter("sessionId", issuerSessionId)
        }.body<JsonObject>().let {
            it["credentialTypes"]!!.jsonArray.let { credentialTypes ->
                credentialTypes shouldHaveSize 1
                credentialTypes.first().jsonPrimitive.content shouldBe "VerifiableId"
            }
            it["id"]?.jsonPrimitive?.content shouldBe issuerSessionId
            it["credentials"] shouldNotBe null
            it["credentials"]!!.jsonArray shouldHaveSize 1
        }
    }

    lateinit var credentialId: String
    "List credentials" {
        authenticatedHttp.get("api/wallet/credentials/list").body<JsonObject>()["list"]?.jsonArray?.let {
            it shouldNotBe null
            it shouldHaveSize 1
            it.first().jsonObject["type"]?.jsonArray?.last()?.jsonPrimitive?.contentOrNull shouldBe "VerifiableId"
            credentialId = it.first().jsonObject["id"]?.jsonPrimitive?.content!!
            credentialId shouldStartWith "urn:uuid:"
        } ?: throw IllegalArgumentException("No list in credential list")
    }

    lateinit var verifierRequestUrl: String
    "Verifier request" {
        val body = http.get("verifier-api/default/presentXDevice") {
            parameter("tenantId", "default")
            parameter("vcType", "VerifiableId")
        }.also { it.status.isSuccess() shouldBe true }.body<Map<String, String>>()
        verifierRequestUrl = body["url"] ?: throw IllegalArgumentException("No url in verifier request")
    }

    lateinit var presentationSessionId: String
    "Start presentation" {
        presentationSessionId = authenticatedHttp.post("api/wallet/presentation/startPresentation") {
            setBody(mapOf("oidcUri" to verifierRequestUrl))
        }.also { it.status.isSuccess() shouldBe true }.bodyAsText()
    }

    "Continue presentation" {
        authenticatedHttp.get("api/wallet/presentation/continue") {
            parameter("sessionId", presentationSessionId)
            parameter("did", did)
        }.also { it.status.isSuccess() shouldBe true }.body<JsonObject>().let {
            it["presentableCredentials"]!!.jsonArray.also { it shouldHaveSize 1 }.first().jsonObject.let {
                it["claimId"]?.jsonPrimitive?.content shouldBe "1"
                it["credentialId"]?.jsonPrimitive?.contentOrNull shouldBe credentialId
            }
        }
    }

    lateinit var accessToken: String
    "Fulfill presentation" {
        authenticatedHttp.post("api/wallet/presentation/fulfill") {
            parameter("sessionId", presentationSessionId)
            setBody(listOf(mapOf("claimId" to "1", "credentialId" to credentialId)))
        }.body<JsonObject>().also { it["fulfilled"]?.jsonPrimitive?.boolean shouldBe true }["state"].let {
            accessToken = it!!.jsonPrimitive.content
        }
    }

    "Auth" {
        http.get("verifier-api/default/auth") {
            parameter("access_token", accessToken)
        }.body<JsonObject>().let {
            it["isValid"]?.jsonPrimitive?.boolean shouldBe true
            it["vps"]!!.jsonArray.map { it.jsonObject }
                .map { it["verification_result"]!!.jsonObject["policyResults"]!!.jsonObject }
                .flatMap {
                    it.jsonObject.entries
                }
                .forEach {
                    val b = it.value.jsonObject["isSuccess"]?.jsonPrimitive?.booleanOrNull
                    println("${it.key}: $b")
                    b shouldBe true
                }
        }
    }
})
