package id.walt.webwallet.backend.clients.metaco

import com.metaco.harmonize.HarmonizeContext

object MetacoClient {
    private val signer = WaltIdSandboxSigner()

    fun init() {
        val harmonizeCtx = HarmonizeContext.load(signer, "src/main/resources/harmonize.json");
        val harmonize = harmonizeCtx.harmonize;
        println(harmonize.domains().rootDomain)
    }
}