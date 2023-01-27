package id.walt.multitenancy

data class TenantId(val type: TenantType, val id: String) {
    override fun toString(): String {
        return "$type/$id"
    }

    override fun equals(other: Any?): Boolean {
        return other is TenantId && type == other.type && id == other.id
    }

    companion object {
        const val DEFAULT_TENANT = "default"
    }
}
