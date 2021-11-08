package id.walt.verifier.backend

import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object VerifierController {
    val routes
        get() =
            path("") {
                path("wallets") {
                    get("list", documented(
                        document().operation {
                            it.summary("List wallet configurations")
                                .addTagsItem("verifier")
                                .operationId("listWallets")
                        }
                            .jsonArray<WalletConfiguration>("200"),
                        VerifierController::listWallets,
                    ))
                }
                path("present") {
                    get("vid", documented(
                        document().operation {
                            it.summary("Present Verifiable ID")
                                .addTagsItem("verifier")
                                .operationId("presentVID")
                        }
                            .queryParam<String>("walletId")
                            .result<String>("302"),
                        VerifierController::presentVid
                    ))
                }
                path("verify") {
                    post("{nonce}", documented(
                        document().operation {
                            it.summary("SIOPv2 request verification callback")
                                .addTagsItem("verifier")
                                .operationId("verifySIOPv2Request")
                        }
                            .formParamBody<String> { }
                            .result<String>("302"),
                        VerifierController::verifySIOPv2Request
                    ))
                }
            }

    fun listWallets(ctx: Context) {
        ctx.json(VerifierConfig.config.wallets.values)
    }

    fun presentVid(ctx: Context) {
        val walletId = ctx.queryParam("walletId")
        if (walletId.isNullOrEmpty() || !VerifierConfig.config.wallets.contains(walletId)) {
            ctx.status(HttpCode.BAD_REQUEST).result("Unknown wallet ID given")
        } else {
            val wallet = VerifierConfig.config.wallets[walletId]!!
            ctx.status(HttpCode.FOUND).header(
                "Location", "${wallet.url}/${wallet.presentPath}" +
                        "?${
                            SIOPv2RequestManager.newRequest("https://www.w3.org/2018/credentials/v1/VerifiableId")
                                .toUriQueryString()
                        }"
            )
        }
    }

    fun verifySIOPv2Request(ctx: Context) {
        // TODO: verify siop response
        ctx.status(HttpCode.FOUND).header("Location", "${VerifierConfig.config.verifierUiUrl}/success/")
    }
}
