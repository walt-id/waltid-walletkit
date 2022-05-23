package id.walt.onboarding.backend

import id.walt.BaseApiTest
import id.walt.webwallet.backend.auth.AuthController
import io.javalin.apibuilder.ApiBuilder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class OnboardingApiTests : BaseApiTest() {

    override fun loadRoutes() {
        ApiBuilder.path("api") {
            AuthController.routes
        }
        ApiBuilder.path("onboarding-api") {
            OnboardingController.routes
        }
    }

    @Test()
    fun testGenerateDomainVerificationCode() = runBlocking {
        val userInfo = authenticateDid()
        val code = client.post("$url/onboarding-api/domain/generateDomainVerificationCode"){
            header("Authorization", "Bearer ${userInfo.token}")
            accept(ContentType("plain", "text"))
            contentType(ContentType.Application.Json)
            setBody(mapOf("domain" to "issuer.ssikit.org"))
        }.bodyAsText()
        println(code)
        code shouldHaveLength 68
        code shouldBe DomainOwnershipService.generateWaltIdDomainVerificationCode("issuer.ssikit.org", did)
    }

    @Test()
    fun testCheckDomainVerificationCodeSuccess() = runBlocking {
        val userInfo = authenticateDid()
        val result = client.post("$url/onboarding-api/domain/checkDomainVerificationCode"){
            header("Authorization", "Bearer ${userInfo.token}")
            accept(ContentType("plain", "text"))
            contentType(ContentType.Application.Json)
            setBody(mapOf("domain" to "issuer.ssikit.org"))
        }.body<Boolean>()
        result shouldBe true
    }

    @Test()
    fun testCheckDomainVerificationCodeFail() = runBlocking {
        val userInfo = authenticateDid()
        val result = client.post("$url/onboarding-api/domain/checkDomainVerificationCode"){
            header("Authorization", "Bearer ${userInfo.token}")
            accept(ContentType("plain", "text"))
            contentType(ContentType.Application.Json)
            setBody(mapOf("domain" to "example.com"))
        }.body<Boolean>()
        result shouldBe false
    }
}

