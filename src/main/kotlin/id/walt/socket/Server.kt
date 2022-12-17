package id.walt.socket

import id.walt.WALTID_WALLET_SOCKET_PORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.net.ServerSocket
import java.net.Socket
import java.util.*

//TODO: make it cancelable
class Server {
    private val logger = KotlinLogging.logger {}

    suspend fun start() = withContext(Dispatchers.IO) {
        val server = ServerSocket(WALTID_WALLET_SOCKET_PORT)
        logger.info("Server running on port ${server.localPort}")
        try {
            while (true) {
                val socket = server.accept()
                logger.info("New connection: ${socket.inetAddress.hostAddress}")
                launch { handleConnection(socket) }
            }
        } catch (e: Exception) {
            logger.error("Server exception: ${e.message}")
        } finally {
            logger.info("Closing server: ${server.inetAddress}:${server.localPort}")
            server.close()
        }
    }

    private suspend fun handleConnection(socket: Socket) = withContext(Dispatchers.IO) {
        try {
            val scanner = Scanner(socket.inputStream)
            while (scanner.hasNextLine()) {
                logger.info(scanner.nextLine())
            }
        } catch (e: Exception) {
            logger.error("Socket exception: ${e.message}")
        } finally {
            logger.info("Closing connection: ${socket.inetAddress.hostAddress}")
            socket.close()
        }
    }
}