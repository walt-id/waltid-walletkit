package id.walt.webwallet.backend.quick.setup

import io.javalin.apibuilder.ApiBuilder
import io.javalin.http.Context
import io.javalin.http.HttpCode
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object QuickSetupController {
    val routes
        get() = ApiBuilder.path("") {
            ApiBuilder.post("run", documented(
                document().operation {
                    it.summary("Run the quick-setup. Will create the issuer and verifier tenants and create a did for each")
                        .operationId("run").addTagsItem("Quick-Setup")
                }.body<QuickSetupRequest> {
                    it.description(
                        "{<br/>" +
                                "\"hosts\":[]<br/>" +
                                "}"
                    )
                }.json<QuickConfig>("201"),
                QuickSetupController::run
            )
            )
        }

    private fun run(ctx: Context) = runCatching {
        QuickSetup.run(ctx.bodyAsClass<QuickSetupRequest>().hosts)
    }.onSuccess {
        ctx.status(HttpCode.CREATED)
        ctx.json(it)
    }.onFailure {
        ctx.status(HttpCode.INTERNAL_SERVER_ERROR)
        ctx.json(it.localizedMessage)
    }
}