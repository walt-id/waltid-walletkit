package id.walt.multitenancy

import id.walt.common.KlaxonWithConverters
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented

object MultitenancyController {
    val routes
        get() = path("multitenancy") {
            get("listTenants", documented(
                document().operation {
                    it.summary("List multitenancy registered tenants")
                        .addTagsItem("Multitenancy")
                        .operationId("listTenants")
                }
                    .json<List<TenantId>>("200"),
                MultitenancyController::listTenants
            ))
        }

    private fun listTenants(ctx: Context) {
        val contextsJson = TenantContextManager.listContexts()
        val json = KlaxonWithConverters().toJsonString(contextsJson)

        ctx.status(200).result(json).contentType(ContentType.APPLICATION_JSON)
    }
}
