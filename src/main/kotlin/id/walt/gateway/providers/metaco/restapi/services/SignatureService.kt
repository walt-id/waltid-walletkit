package id.walt.gateway.providers.metaco.restapi.services

interface SignatureService<T> {
    fun sign(payload: T): String
}