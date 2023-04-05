package id.walt.multitenancy

import id.walt.common.KlaxonWithConverters
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.plugin.openapi.dsl.document
import io.javalin.plugin.openapi.dsl.documented
import kotlinx.serialization.Serializable

object MultitenancyController {

    @Serializable
    data class MultitenancyResult(val success: Boolean)

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

            delete(
                "unloadTenant/{TenantType}/{TenantId}", documented(
                    document().operation {
                        it.summary("Unload a *currently loaded* tenant. Available TenantTypes: ${TenantType.values().map { it.name }}")
                            .addTagsItem("Multitenancy")
                            .operationId("unloadTenant")
                    }.json<MultitenancyResult>("200"),
                    MultitenancyController::unloadTenant
                )
            )

            delete(
                "deleteTenant/{TenantType}/{TenantId}", documented(
                    document().operation {
                        it.summary("Delete a tenant. Available TenantTypes: ${TenantType.values().map { it.name }}")
                            .addTagsItem("Multitenancy")
                            .operationId("deleteTenant")
                    }.json<MultitenancyResult>("200"),
                    MultitenancyController::deleteTenant
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

    private fun unloadTenant(ctx: Context) {
        val type = ctx.pathParam("TenantType")
        val id = ctx.pathParam("TenantId")

        val res = TenantContextManager.unloadContext(type, id)

        ctx.json(MultitenancyResult(res))
    }

    private fun deleteTenant(ctx: Context) {
        val type = ctx.pathParam("TenantType")
        val id = ctx.pathParam("TenantId")

        val res = TenantContextManager.deleteContext(type, id)

        ctx.json(MultitenancyResult(res))
    }
}
