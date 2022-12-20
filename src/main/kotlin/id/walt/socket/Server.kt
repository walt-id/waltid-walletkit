package id.walt.socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import java.util.Objects.requireNonNull
import javax.net.ssl.*


//TODO: make it cancelable
class Server {
    private val logger = KotlinLogging.logger {}

    init {
        System.setProperty("javax.net.debug", "ssl")
    }

    suspend fun start(
        port: Int,
        keyStore: StoreParameter,
        trustStore: StoreParameter,
        tlsVersion: String = "TLSv1.3",
    ) = withContext(Dispatchers.IO) {
        requireNonNull(tlsVersion, "TLS version is mandatory")
        require(port > 0) { "Port number cannot be less than or equal to 0" }
        initContext(port, trustStore, keyStore).use {it as SSLServerSocket
            it.needClientAuth = true
            it.enabledProtocols = arrayOf(tlsVersion)
//            it.enabledProtocols = it.supportedProtocols
//            it.enabledCipherSuites = it.supportedCipherSuites
//            it.needClientAuth = false
//            it.wantClientAuth = false
//            it.useClientMode = false
            logger.info("Server running on port ${it.localPort}")
            listen(it)
//            while (true) {
//                val socket = it.accept() as SSLSocket
//                logger.info("New connection: ${socket.inetAddress.hostAddress}")
//                launch { handleConnection(socket) }
//                socket.addHandshakeCompletedListener {
//                    //start to communicate
//                    logger.info { "Handshake completed for ${it.peerPrincipal.name}" }
//                    launch { handleConnection(socket) }
//                }
//                socket.startHandshake()
//            }
        }
    }

    suspend fun startUnsecure(port: Int) = withContext(Dispatchers.IO) {
        require(port > 0) { "Port number cannot be less than or equal to 0" }
        val server = ServerSocket(port)
        logger.info("Server running on port ${server.localPort}")
        try {
            listen(server)
        } catch (e: Exception) {
            logger.error("Server exception: ${e.message}")
        } finally {
            logger.info("Closing server: ${server.inetAddress}:${server.localPort}")
            server.close()
        }
    }

    private suspend fun listen(server: ServerSocket) = withContext(Dispatchers.IO) {
        while (true) {
            val socket = server.accept()
            logger.info("New connection: ${socket.inetAddress.hostAddress}")
            launch { handleConnection(socket) }
        }
    }

    private fun initContext(port: Int, trustStore: StoreParameter, keyStore: StoreParameter): ServerSocket = let {
        // create ssl context
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(
            initKeyManagers(keyStore.filepath, keyStore.password), initTrustManagers(trustStore.filepath, trustStore.password),
            SecureRandom.getInstanceStrong()
        )
        ctx.serverSocketFactory.createServerSocket(port)
    }

    private fun initKeyManagers(path: String, keyStorePassword: CharArray?): Array<KeyManager> = let {
        KeyStore.getInstance(KeyStore.getDefaultType()).run {
            File(path).inputStream().use {
                this.load(it, keyStorePassword)
            }.let {
                val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                kmf.init(this, keyStorePassword)
                kmf.keyManagers
            }
        }
    }

    private fun initTrustManagers(path: String, trustStorePassword: CharArray?): Array<TrustManager> = let {
        KeyStore.getInstance(KeyStore.getDefaultType()).run {
            File(path).inputStream().use {
                this.load(it, trustStorePassword)
            }.let {
                val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                tmf.init(this)
                tmf.trustManagers
            }
        }
    }

    private suspend fun handleConnection(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            val scanner = Scanner(socket.inputStream)
            while (scanner.hasNextLine()) {
                logger.debug(scanner.nextLine())
            }
        } catch (e: Exception) {
            logger.error("Socket exception: ${e.message}")
        } finally {
            logger.info("Closing connection: ${socket.inetAddress.hostAddress}")
            socket.close()
        }
    }
}