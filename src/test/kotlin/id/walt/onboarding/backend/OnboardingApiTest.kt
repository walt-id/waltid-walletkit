package id.walt.onboarding.backend

import id.walt.BaseApiTest
import id.walt.webwallet.backend.auth.AuthController
import io.javalin.apibuilder.ApiBuilder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.request.*
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
        val userInfo = authenticate()
        val code = client.post<String>("$url/onboarding-api/domain/generateDomainVerificationCode"){
            header("Authorization", "Bearer ${userInfo.token}")
            accept(ContentType("plain", "text"))
            contentType(ContentType.Application.Json)
            body = mapOf("domain" to "waltid.org")
        }
        code shouldHaveLength 61
        code shouldBe "walt-id-verification=6307228575B8DFFDE096FFDA554D15B7261970F3"
    }
}