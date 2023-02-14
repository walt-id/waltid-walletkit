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
            get(
                "listLoadedTenants", documented(
                    document().operation {
                        it.summary("List multitenancy *LOADED* tenants. If no tenants are loaded (e.g. right after a restart) this method will indeed return an empty list.")
                            .addTagsItem("Multitenancy")
                            .operationId("listLoadedTenants")
                    }.json<List<TenantId>>("200"),
                    MultitenancyController::listLoadedTenants
                )
            )

            get(
                "listAllTenantIdsByType/{TenantType}", documented(
                    document().operation {
                        it.summary(
                            "List multitenancy tenant IDs by tenant type. Available tenant types: ${
                                TenantType.values().map { it.name }
                            }"
                        )
                            .addTagsItem("Multitenancy")
                            .operationId("listAllTenantIdsByType")
                    }.json<List<String>>("200"),
                    MultitenancyController::listAllTenantIdsByType
                )
            )
        }

    private fun listLoadedTenants(ctx: Context) {
        val contextsJson = TenantContextManager.listLoadedContexts()
        val json = KlaxonWithConverters().toJsonString(contextsJson)

        ctx.status(200).result(json).contentType(ContentType.APPLICATION_JSON)
    }

    private fun listAllTenantIdsByType(ctx: Context) {
        val tenantType = ctx.pathParam("TenantType")
        ctx.json(TenantContextManager.listAllContextIdsByType(TenantType.valueOf(tenantType)))
    }
}
