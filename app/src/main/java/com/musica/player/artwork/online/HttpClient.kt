package com.musica.player.artwork.online

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Respuesta de una petición HTTP: código de estado y cuerpo en bytes. */
class HttpResponse(val code: Int, val body: ByteArray) {

    val isSuccessful: Boolean get() = code in 200..299

    /** Interpreta el cuerpo como texto (para las respuestas JSON). */
    fun bodyText(): String = body.toString(Charsets.UTF_8)
}

/**
 * Cliente HTTP mínimo. Se abstrae para poder sustituirlo en los tests sin tocar
 * la red.
 */
interface HttpClient {
    /** Realiza un GET de [url]; lanza si no se puede conectar. */
    suspend fun get(url: String): HttpResponse
}

/**
 * Implementación con [HttpURLConnection]: no añade dependencias externas. Los
 * timeouts evitan que una red lenta alargue la carga de la carátula.
 */
internal class UrlConnectionHttpClient(
    private val userAgent: String,
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 15_000,
) : HttpClient {

    override suspend fun get(url: String): HttpResponse = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            setRequestProperty("User-Agent", userAgent)
            setRequestProperty("Accept", "application/json, image/*")
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { it.readBytes() } ?: ByteArray(0)
            HttpResponse(code, body)
        } finally {
            connection.disconnect()
        }
    }
}
