package id.walt.multitenancy

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import id.walt.WALTID_DATA_ROOT
import id.walt.services.hkvstore.FilesystemStoreConfig
import mu.KotlinLogging

object FileSystemStoreConfigCreator {

    private val log = KotlinLogging.logger {  }

    private val maxKeySize by lazy {
        val config = ConfigLoaderBuilder.default()
            .addResourceSource("config/fsStore.conf")
            .build()
            .loadConfig<FilesystemStoreConfig>()

        val maxKeySize = if (config.isValid()) {
            config.getUnsafe().maxKeySize
        } else 111

        log.debug { "Max key size before hashing has been set to: $maxKeySize" }

        maxKeySize
    }

    fun makeFileSystemStoreConfig(key: String): FilesystemStoreConfig {
        val dataRoot = "$WALTID_DATA_ROOT/data/${key}"

        log.debug { "Making file system store config: $dataRoot" }

        return FilesystemStoreConfig(dataRoot, maxKeySize)
    }

}
