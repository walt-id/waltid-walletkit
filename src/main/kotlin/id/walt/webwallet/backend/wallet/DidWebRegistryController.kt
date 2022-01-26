package id.walt.webwallet.backend.wallet

import id.walt.model.DidWeb
import id.walt.services.did.DidService
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object DidWebRegistryController {
    val routes
        get() =
            path("{id}/did.json") {
                get("", documented(
                    document().operation {
                        it.summary("Load did web")
                            .addTagsItem("did-web")
                            .operationId("loadDid")
                    }
                        .json<DidWeb>("200"),
                    DidWebRegistryController::loadDidWeb
                ))
            }

    private fun loadDidWeb(ctx: Context) {
        val id = ctx.pathParam("id")
        ctx.json(DidService.load("did:web:wallet.walt.id:$id"))
    }

}
