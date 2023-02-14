package id.walt.multitenancy


interface TenantConfigFactory<C : TenantConfig> {
    fun fromJson(json: String): C
    fun forDefaultTenant(): C
}
