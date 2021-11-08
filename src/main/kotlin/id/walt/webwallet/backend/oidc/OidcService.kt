package id.walt.webwallet.backend.oidc

import id.walt.webwallet.backend.oidc.requests.*
import id.walt.webwallet.backend.oidc.responses.OidcConsentResponse
import id.walt.webwallet.backend.oidc.responses.OidcTokenResponse
import id.walt.webwallet.backend.oidc.responses.OidcUserInfoResponse
import org.slf4j.LoggerFactory


object OidcService {

    private val log = LoggerFactory.getLogger(OidcService::class.java)

    //fun insertClient(client: OidcClient) = DatabaseManager.oidc.insertDocument(client)

    //fun getById(id: String) = DatabaseManager.oidc.getDocument(id, OidcClient::class.java)

    // TODO replace with session cache
    var authReq: OidcAuthenticationRequest? = null

    //fun register(req: OidcRegisterRequest) = OidcClient.newClient(req).also { insertClient(it) }

    /**
     * Returns the Authentication Endpoint or an Error Response URL
     */
    fun authorize(req: OidcAuthenticationRequest): String {
        log.info("Authentication Request:  $req")

        // TODO check if user is authenticated already. if not -> login; else -> consent;
        val authenticationResponse = "https://letstrust.id/user/login?scope=openid"

        // TODO error handling according to https://openid.net/specs/openid-connect-core-1_0.html#AuthError
        // If the redirect_uri is valid, the error-codes will be sent to this page
        val authenticationErrorResponse: String? =
            null // e.g. HTTP/1.1 302 Found  Location: https://client.example.org/cb?error=invalid_request&error_description=Unsupported%20response_type%20value&state=af0ifjsldkj

        // check if the client_id is valid
        //val client = loadCheckedClient(req)

        // check if redirect_uri matches the pre-registered one
        //if (client.redirectUris.contains(req.redirectUri)) throw java.lang.IllegalArgumentException("Invalid redirect URI")

        // TODO: check if the scope + response_type are valid

        // TODO: Should be stored in session-cache
        authReq = req

        return authenticationErrorResponse ?: authenticationResponse
    }

//    private fun loadCheckedClient(req: OidcAuthenticationRequest): OidcClient {
//        if (req.clientId.isNullOrEmpty())
//            error("invalid client ID: ${req.clientId}")
//
//        return getById(req.clientId!!) ?: error("Could not load OIDC client by ID ${req.clientId}")
//    }

    fun consent(req: OidcConsentRequest): OidcConsentResponse {
        // TODO generate code and store it within the user session
        val code = "SplxlOBeZQQYbYS6WxSbIA"
        val oidcAuthenticationResponseUrl = "${authReq!!.redirectUri}?code=${code}&state=${authReq!!.state}"

        log.info("Redirecting to:  $oidcAuthenticationResponseUrl")

        // TODO consider error handling
        return OidcConsentResponse(oidcAuthenticationResponseUrl)
    }

    fun token(req: OidcTokenRequest): OidcTokenResponse {
        // TODO Validate Token Request https://openid.net/specs/openid-connect-core-1_0.html#TokenRequestValidation

        // TODO validate client_id and client_secret

        // TODO validate code (may only be used once and has to expire within 10min) https://tools.ietf.org/html/rfc6749#section-4.1.2

        // TODO validate and redirect_url

        // TODO error handling

        // TODO generate Access Token

        // TODO generate ID_TOKEN

        return OidcTokenResponse(
            "SlAV32hkKG",
            "Bearer",
            "",
            3600,
            "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzcyI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZfV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5NzAKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6qJp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJNqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7TpdQyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoSK5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg"
        )
//        return mapOf(
//            "id_token" to "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzcyI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZfV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5NzAKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6qJp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJNqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7TpdQyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoSK5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg",
//            "access_token" to "SlAV32hkKG",
//            "token_type" to "Bearer",
//            "expires_in" to "3600"
//        )
    }

    fun introspect(req: OidcIntrospecRequest): Map<String, Any> {
        // TODO implement
        return mapOf(
            "active" to true,
            "scope" to "read write email",
            "client_id" to "SlAV32hkKG",
            "username" to "phil",
            "exp" to 1437275311,
        )
    }

    fun revoke(req: OidcRevokeRequest): Int {
        // TODO implement
        return 200
    }

    // https://openid.net/specs/openid-connect-core-1_0.html#UserInfo
    fun userinfo(): OidcUserInfoResponse { //Map<String, Any> {
        // TODO validate bearer token

        // TODO fetch user details from database

        // TODO Build and sign response according to https://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponse

        val resp = OidcUserInfoResponse("12349876")

        resp.setMap(
            hashMapOf(
                "email" to "dominik.beron@letstrust.io",
                "email_verified" to true,
                "picture" to "https://letstrust.id/assets/img/demo/profile_img.png",
                "name" to "Dominik Beron",

                "address" to mapOf(
                    "street_address" to "123 Hollywood Blvd.",
                    "locality" to "Los Angeles",
                    "region" to "CA",
                    "postal_code" to "90210",
                    "country" to "US"
                ),

                "skills" to mapOf(
                    "java" to listOf("1", "2"), // CredentialIDs should be UUIDs
                    "php" to listOf("3", "4"),
                    "oop" to listOf("1", "3")
                ),

                "education" to listOf("5", "6"),
                "work_history" to listOf("7", "8")
            )
        )

        /*
        resp.add(
            "address", mapOf(
                "street_address" to "123 Hollywood Blvd.",
                "locality" to "Los Angeles",
                "region" to "CA",
                "postal_code" to "90210",
                "country" to "US"
            )
        )*/

        return resp

//        return mapOf(
//            "sub" to "alice",
//            "email" to "alice@wonderland.net",
//            "email_verified" to true,
//            "name" to "Alice Adams",
//            "picture" to "https://c2id.com/users/alice.jpg",
//            "address" to mapOf(
//                "street_address" to "123 Hollywood Blvd.",
//                "locality" to "Los Angeles",
//                "region" to "CA",
//                "postal_code" to "90210",
//                "country" to "US"),
//            "skills" to mapOf(
//                "java" to listOf("1", "2"), // CredentialIDs should be UUIDs
//                "php" to listOf("3", "4"),
//                "oop" to listOf("1", "3")
//            ),
//            "education" to listOf("5", "6"),
//            "work_history" to listOf("7", "8"),
//        )
    }
}
