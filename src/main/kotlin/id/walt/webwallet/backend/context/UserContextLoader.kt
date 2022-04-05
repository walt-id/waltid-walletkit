package id.walt.webwallet.backend.context

import com.google.common.cache.CacheLoader
import id.walt.services.context.Context
import id.walt.services.hkvstore.InMemoryHKVStore
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService
import id.walt.WALTID_DATA_ROOT
import java.security.MessageDigest

object UserContextLoader : CacheLoader<String, Context>() {

  private fun hashString(input: String): String {
    return MessageDigest
      .getInstance("SHA-256")
      .digest(input.toByteArray())
      .fold("", { str, it -> str + "%02x".format(it) })
  }

  override fun load(key: String): UserContext {
    //TODO: get user context preferences from user database
//    return UserContext(key, HKVKeyStoreService(), HKVVcStoreService(), FileSystemHKVStore(FilesystemStoreConfig("${WALTID_DATA_ROOT}/data/${key}")))
    return UserContext(key, HKVKeyStoreService(), HKVVcStoreService(), InMemoryHKVStore())
  }
}
