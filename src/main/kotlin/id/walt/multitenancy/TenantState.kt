package id.walt.multitenancy

interface TenantState<C : TenantConfig> {
    var config: C?
}
