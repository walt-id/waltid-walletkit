package id.walt.multitenancy

interface TenantConfig {
    fun toJson(): String
}
