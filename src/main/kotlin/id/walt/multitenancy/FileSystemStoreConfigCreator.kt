package id.walt.multitenancy

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import id.walt.WALTID_DATA_ROOT
import id.walt.services.hkvstore.FilesystemStoreConfig

object FileSystemStoreConfigCreator {

    fun makeFileSystemStoreConfig(key: String): FilesystemStoreConfig {
        val config = ConfigLoaderBuilder.default()
            .addResourceSource("config/fsStore.conf")
            .build()
            .loadConfig<FilesystemStoreConfig>()

        val maxKeySize = if (config.isValid()) {
            config.getUnsafe().maxKeySize
        } else 111

        return FilesystemStoreConfig("$WALTID_DATA_ROOT/data/${key}", maxKeySize)
    }

}
