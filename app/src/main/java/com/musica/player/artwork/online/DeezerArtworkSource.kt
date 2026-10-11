package com.musica.player.artwork.online

import java.net.URLEncoder
import java.text.Normalizer
import org.json.JSONObject

/**
 * Carátulas de álbum desde la API pública de Deezer (sin clave ni cuenta).
 *
 * [DeezerArtworkSource.findAlbumArtwork] busca por texto libre ("artista álbum")
 * y, entre los resultados, elige el que mejor coincide con el álbum y el
 * artista pedidos; así se evita mostrar una carátula equivocada cuando el
 * nombre es ambiguo. Después descarga la imagen de mayor resolución
 * (`cover_xl`).
 */
internal class DeezerArtworkSource(private val http: HttpClient) : OnlineArtworkSource {

    override suspend fun findAlbumArtwork(album: String, artist: String): OnlineArtwork {
        if (album.isBlank()) return OnlineArtwork.NotFound

        val query = listOf(artist, album)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
        val search = request("$SEARCH_URL?q=${encode(query)}&limit=$SEARCH_LIMIT")
            ?: return OnlineArtwork.Error
        if (!search.isSuccessful) return OnlineArtwork.Error

        val coverUrl = try {
            selectCoverUrl(search.bodyText(), album, artist)
        } catch (e: org.json.JSONException) {
            // Respuesta inesperada del servicio: se reintentará más adelante.
            return OnlineArtwork.Error
        } ?: return OnlineArtwork.NotFound

        val image = request(coverUrl) ?: return OnlineArtwork.Error
        if (!image.isSuccessful || image.body.isEmpty()) return OnlineArtwork.Error
        return OnlineArtwork.Found(image.body)
    }

    /** Devuelve la respuesta o null si la petición no se pudo completar. */
    private suspend fun request(url: String): HttpResponse? =
        runCatching { http.get(url) }.getOrNull()

    /**
     * Elige la URL de carátula del resultado que mejor coincide con [album] y
     * [artist]. Devuelve null si ninguno encaja (para no mostrar arte erróneo).
     */
    private fun selectCoverUrl(json: String, album: String, artist: String): String? {
        val root = JSONObject(json)
        if (root.has("error")) return null
        val data = root.optJSONArray("data") ?: return null

        val targetAlbum = normalize(album)
        if (targetAlbum.isEmpty()) return null
        val targetArtist = normalize(artist)

        var bestUrl: String? = null
        var bestScore = 0
        for (i in 0 until data.length()) {
            val entry = data.optJSONObject(i) ?: continue
            val albumName = normalize(entry.optString("title"))
            val artistName = normalize(entry.optJSONObject("artist")?.optString("name").orEmpty())
            val score = score(targetAlbum, targetArtist, albumName, artistName)
            if (score > bestScore) {
                bestScore = score
                bestUrl = coverUrl(entry)
            }
        }
        return if (bestScore >= MIN_SCORE) bestUrl else null
    }

    /**
     * Puntúa una coincidencia. El nombre del álbum debe aparecer siempre; si hay
     * artista, su nombre también debe solaparse. Los nombres exactos puntúan más
     * que una simple contención, de modo que "Album" gana a "Album (Deluxe)".
     */
    private fun score(
        targetAlbum: String,
        targetArtist: String,
        albumName: String,
        artistName: String,
    ): Int {
        val albumScore = when {
            albumName == targetAlbum -> 2
            albumName.contains(targetAlbum) || targetAlbum.contains(albumName) -> 1
            else -> return 0
        }
        val artistScore = when {
            targetArtist.isEmpty() -> 0
            artistName == targetArtist -> 2
            artistName.contains(targetArtist) || targetArtist.contains(artistName) -> 1
            else -> return 0
        }
        return albumScore * 2 + artistScore
    }

    private fun coverUrl(entry: JSONObject): String? =
        COVER_FIELDS.firstNotNullOfOrNull { field ->
            entry.optString(field).takeIf { it.isNotBlank() }
        }

    /** Normaliza para comparar: minúsculas, sin acentos ni puntuación. */
    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace(NON_ALPHANUMERIC, " ")
            .trim()

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")

    private companion object {
        const val SEARCH_URL = "https://api.deezer.com/search/album"
        const val SEARCH_LIMIT = 10
        const val MIN_SCORE = 2
        val COVER_FIELDS = listOf("cover_xl", "cover_big", "cover_medium", "cover")
        val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")
    }
}
