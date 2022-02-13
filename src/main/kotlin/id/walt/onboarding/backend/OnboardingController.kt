package id.walt.onboarding.backend

import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

data class GenerateDomainVerificationCodeRequest(val domain: String)
data class CheckDomainVerificationCodeRequest(val domain: String)

object OnboardingController {
    val routes
        get() = path("domain") {
            path("generateDomainVerificationCode") {
                post("", documented(
                    document().operation {
                        it.summary("Generate domain verification code")
                            .addTagsItem("Onboarding")
                            .operationId("generateDomainVerificationCode")
                    }
                        .body<GenerateDomainVerificationCodeRequest>()
                        .result<String>("200"),
                    OnboardingController::generateDomainVerificationCode
                ))
            }
            path("checkDomainVerificationCode") {
                post("", documented(
                    document().operation {
                        it.summary("Check domain verification code")
                            .addTagsItem("Onboarding")
                            .operationId("checkDomainVerificationCode")
                    }
                        .body<CheckDomainVerificationCodeRequest>()
                        .result<Boolean>("200"),
                    OnboardingController::checkDomainVerificationCode
                ))
            }
        }

    private fun generateDomainVerificationCode(ctx: Context) {
        val domainReq = ctx.bodyAsClass<GenerateDomainVerificationCodeRequest>()
        ctx.result(DomainOwnershipService.generateWaltIdDomainVerificationCode(domainReq.domain))
    }

    private fun checkDomainVerificationCode(ctx: Context) {
        val domainReq = ctx.bodyAsClass<CheckDomainVerificationCodeRequest>()
        ctx.json(DomainOwnershipService.checkWaltIdDomainVerificationCode(domainReq.domain))
    }
}