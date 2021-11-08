package id.walt.webwallet.backend.context

import id.walt.services.hkvstore.HKVStoreService
import id.walt.services.keystore.KeyStoreService
import id.walt.services.vcstore.VcStoreService

class UserContext(
    val keyStore: KeyStoreService,
    val vcStore: VcStoreService,
    val hkvStore: HKVStoreService
)
