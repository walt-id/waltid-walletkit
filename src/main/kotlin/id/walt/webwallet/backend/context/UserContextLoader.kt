package id.walt.webwallet.backend.context

import com.google.common.cache.CacheLoader
import id.walt.services.hkvstore.FileSystemHKVStore
import id.walt.services.hkvstore.FilesystemStoreConfig
import id.walt.services.hkvstore.InMemoryHKVStore
import id.walt.services.keystore.HKVKeyStoreService
import id.walt.services.vcstore.HKVVcStoreService

object UserContextLoader : CacheLoader<String, UserContext>() {
  override fun load(key: String): UserContext {
    return UserContext(HKVKeyStoreService(), HKVVcStoreService(), FileSystemHKVStore(FilesystemStoreConfig("data/${key}")))
  }
}