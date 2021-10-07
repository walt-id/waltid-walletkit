package id.walt.webwallet.backend.oidc

import id.walt.webwallet.backend.oidc.requests.*
import id.walt.webwallet.backend.oidc.responses.OidcTokenResponse
import id.walt.webwallet.backend.oidc.responses.OidcUserInfoResponse
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import io.javalin.plugin.openapi.dsl.document

object OidcController {

    fun authorize(ctx: Context) {
        ctx.redirect(
            OidcService.authorize(
                OidcAuthenticationRequest(
                    ctx.queryParam("scope"),
                    ctx.queryParam("response_type"),
                    ctx.queryParam("client_id"),
                    ctx.queryParam("redirect_uri"),
                    ctx.queryParam("state")
                )
            )
        )
    }

    fun authorizeDocs() = document().operation {
        it.summary("Authorization Endpoint").operationId("authorize").addTagsItem("OIDC")
            .description(
                "Example request would look like this: " +
                        "http://localhost:7000/oidc/authorize?response_type=code&scope=openid&client_id=s6BhdRkqt3&state=af0ifjsldkj&redirect_uri=http:%2F%2Flocalhost:63342%2Fletstrust-web%2Fsrc%2Fassets%2Fstatic%2Fdemo%2Fsuccess.html",
            )
    }.pathParam<String>("response_type").pathParam<String>("scope").pathParam<String>("client_id")
        .pathParam<String>("state").pathParam<String>("redirect_uri").result<String>("302")

    fun consent(ctx: Context) {
        ctx.json(OidcService.consent(OidcConsentRequest("todo")))
    }

//    fun consentDocs() = document().operation {
//        it.summary("Consent Endpoint").operationId("consent").addTagsItem("OIDC")
//    }.body<OidcConsentRequest> { it.description("Stores the consent and returns the code for fetching the ID token. Note that the redirect URL must point to the backend of the RP in order to conduct a call to the token endpoint.") }
//        .json<CreateUserResponse>("200")

    fun token(ctx: Context) {
        ctx.json(
            OidcService.token(
                OidcTokenRequest(
                    ctx.queryParam("grant_type") ?: "",
                    ctx.queryParam("code") ?: "",
                    ctx.queryParam("redirect_uri") ?: ""
                )
            )
        )
    }

    fun tokenDocs() = document().operation {
        it.summary("Token Endpoint").operationId("token").addTagsItem("OIDC")
    }.body<OidcConsentRequest> {
        it.description("Endpoint for retrieving the ID token")
    }.json<OidcTokenResponse>("200")

    fun introspec(ctx: Context) {
        ctx.json(OidcService.introspect(ctx.bodyAsClass(OidcIntrospecRequest::class.java)))
    }

    fun introspecDocs() = document().operation {
        it.summary("Introspect Endpoint").operationId("introspec").addTagsItem("OIDC")
    }.body<OidcConsentRequest> {
        it.description("Endpoint for retrieving status of a given token")
    }.result<String>("200")

    @OpenApi(
        summary = "Revocation Endpoint",
        operationId = "revoke",
        tags = ["OIDC"],
        requestBody = OpenApiRequestBody(
            [OpenApiContent(OidcConsentRequest::class)],
            true,
            "Endpoint for revoking a given token"
        ),
        security = [OpenApiSecurity("http")],
        responses = [
            OpenApiResponse("200"),
//            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)], "invalid request")
        ]
    )
    fun revoke(ctx: Context) {
        val httpStatus: Int = OidcService.revoke(ctx.bodyAsClass(OidcRevokeRequest::class.java))
        ctx.status(httpStatus)
    }

//    @OpenApi(
//        summary = "Client Registration Endpoint",
//        operationId = "register",
//        tags = ["OIDC"],
//        requestBody = OpenApiRequestBody(
//            [OpenApiContent(OidcRegisterRequest::class)],
//            true,
//            "Endpoint for dynamic client registration"
//        ),
//        security = [OpenApiSecurity("http")],
//        responses = [
//            OpenApiResponse("200", [OpenApiContent(OidcClient::class)], "successful"),
////            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)], "invalid request")
//        ]
//    )
//    fun register(ctx: Context) {
//        ctx.json(OidcService.register(ctx.bodyAsClass(OidcRegisterRequest::class.java)))
//    }

    @OpenApi(
        summary = "UserInfo Endpoint",
        operationId = "userinfo",
        tags = ["OIDC"],
        security = [OpenApiSecurity("http")],
        responses = [
            OpenApiResponse("200", [OpenApiContent(OidcUserInfoResponse::class)], "successful"),
//            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)], "invalid request")
        ]
    )
    fun userinfo(ctx: Context) {
        ctx.json(OidcService.userinfo())
        //ctx.json(OidcService.userinfo(ctx.bodyAsClass(OidcUserInfoRequest::class.java)))
    }
}
