package id.walt.webwallet.backend.wallet

import id.walt.model.DidWeb
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.webwallet.backend.WALTID_DATA_ROOT
import id.walt.webwallet.backend.context.UserContext
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object DidWebRegistryController {
    val routes
        get() = path("did-registry") {
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
        }

    val didRegistryContext = UserContext(
        hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/did-registry")),
        keyStore = HKVKeyStoreService(),
        vcStore = HKVVcStoreService()
    )

    private fun loadDidWeb(ctx: Context) {
        val id = ctx.pathParam("id")
        ContextManager.runWith(didRegistryContext) {
            ctx.json(DidService.load("did:web:wallet.walt.id:$id"))
        }
    }

}
