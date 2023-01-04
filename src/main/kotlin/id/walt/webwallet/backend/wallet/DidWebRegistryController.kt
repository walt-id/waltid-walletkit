package id.walt.webwallet.backend.wallet

import id.walt.WALTID_DATA_ROOT
import id.walt.model.Did
import id.walt.model.DidMethod
import id.walt.model.did.DidWeb
import id.walt.services.context.ContextManager
import id.walt.services.did.DidService
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.webwallet.backend.config.WalletConfig
import id.walt.webwallet.backend.context.UserContext
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        contextId = "did registry",
        hkvStore = FileSystemHKVStore(FilesystemStoreConfig("$WALTID_DATA_ROOT/data/did-registry")),
        keyStore = HKVKeyStoreService(),
        vcStore = HKVVcStoreService()
    )

    val domain
        get() = URI.create(WalletConfig.config.walletApiUrl).authority

    val domainDidPart
        get() = domain.let {
            URLEncoder.encode(it, StandardCharsets.UTF_8)
        }

    val rootPath = "api/did-registry"
    val rootPathDidPart = "api:did-registry"

    private fun loadDidWeb(ctx: Context) {
        val id = ctx.pathParam("id")
        ContextManager.runWith(didRegistryContext) {
            try {
                ctx.json(
                    DidService.load(
                        "did:web:$domainDidPart:$rootPathDidPart:${id}"
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ctx.status(404)
            }
        }
    }

    fun registerDidWeb(ctx: Context) {
        val did = Did.decode(ctx.body()) ?: throw BadRequestResponse("Could not parse DID")
        if (did.method != DidMethod.web) throw BadRequestResponse("DID must be of type did:web")
        val match =
            "did:web:${Regex.escape(domainDidPart)}:${Regex.escape(rootPathDidPart)}:([^:]+)".toRegex().matchEntire(did.id)
                ?: throw BadRequestResponse("did:web doesn't match this registry domain and path")
        val id = match.groups[1]!!.value

        ContextManager.runWith(didRegistryContext) {
            if (DidService.listDids().any { d -> d == did.id }) throw BadRequestResponse("DID already registered")

            DidService.storeDid(did.id, did.encode())
            DidService.importKeys(did.id)


        }
    }
}
