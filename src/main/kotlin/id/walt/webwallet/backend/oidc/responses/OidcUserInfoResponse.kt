package id.walt.webwallet.backend.oidc.responses


import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty

class OidcUserInfoResponse(@JsonProperty("sub") val sub: String) {

    private var map = HashMap<String, Any>()

    @JsonAnySetter
    fun add(key: String, value: Any) {
        map[key] = value
    }

    @JsonAnyGetter
    fun getMap(): Map<String, Any> {
        return map
    }

    fun setMap(newMap: HashMap<String, Any>) {
        map = newMap
    }
}
