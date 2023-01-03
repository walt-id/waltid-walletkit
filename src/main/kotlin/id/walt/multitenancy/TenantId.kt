package id.walt.multitenancy

data class TenantId(val tenantType: TenantType, val tenantId: String) {
  override fun toString(): String {
    return "$tenantType/$tenantId"
  }

  override fun equals(other: Any?): Boolean {
    return other is TenantId && tenantType == other.tenantType && tenantId == other.tenantId
  }

  companion object {
    const val DEFAULT_TENANT = "default"
  }
}
