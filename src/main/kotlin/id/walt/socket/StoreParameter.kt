package id.walt.socket

data class StoreParameter(
    val filepath: String,
    val password: CharArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoreParameter

        if (filepath != other.filepath) return false
        if (password != null) {
            if (other.password == null) return false
            if (!password.contentEquals(other.password)) return false
        } else if (other.password != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filepath.hashCode()
        result = 31 * result + (password?.contentHashCode() ?: 0)
        return result
    }
}