package id.walt.gateway.providers.metaco.restapi.signservice

interface SignatureService<T> {
    fun sign(payload: T): String
}